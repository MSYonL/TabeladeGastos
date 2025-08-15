package br.com.projeto.tabelagastos.ui;

import br.com.projeto.tabelagastos.model.Despesa;
import br.com.projeto.tabelagastos.service.DespesaService;
import br.com.projeto.tabelagastos.service.ExcelGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GastosSwingApp {

	private final JFrame frame = new JFrame("Tabela de Gastos");
	private final DefaultTableModel modeloTabela = new DefaultTableModel();
	private final JTable tabela = new JTable(modeloTabela);
	private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabela);
	private final JLabel totalLabel = new JLabel("Total do mês: R$ 0.00");

	private final JTextField filtroMes = new JTextField(5);
	private final JTextField filtroPago = new JTextField(5);

	private final DespesaService despesaService = new DespesaService();
	private final ExcelGenerator excelGenerator = new ExcelGenerator();

	public GastosSwingApp() {
		inicializarUI();
	}

	private void inicializarUI() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(900, 600);
		frame.setLayout(new BorderLayout());

		modeloTabela.setColumnIdentifiers(
				new String[] { "Conta", "Parcelada", "Parcela", "Valor", "Pago", "Mês", "Ano", "Vencimento" });
		tabela.setRowSorter(sorter);

		JScrollPane scrollTabela = new JScrollPane(tabela);

		JButton adicionarBtn = new JButton("Adicionar");
		JButton excluirBtn = new JButton("Excluir");
		JButton editarBtn = new JButton("Editar");
		JButton salvarPlanilhaBtn = new JButton("Salvar Planilha");
		JButton abrirPlanilhaBtn = new JButton("Abrir Planilha");
		JButton aplicarFiltroBtn = new JButton("Aplicar Filtro");
		JButton limparFiltroBtn = new JButton("Limpar Filtros");

		aplicarFiltroBtn.addActionListener(e -> {
			String mes = filtroMes.getText().trim();
			String pago = filtroPago.getText().trim();

			RowFilter<DefaultTableModel, Object> filtroMes = RowFilter.regexFilter("(?i)" + mes, 5);
			RowFilter<DefaultTableModel, Object> filtroPago = RowFilter.regexFilter("(?i)" + pago, 4);

			sorter.setRowFilter(RowFilter.andFilter(List.of(filtroMes, filtroPago)));
			calcularTotalMes();
		});

		limparFiltroBtn.addActionListener(e -> {
			filtroMes.setText("");
			filtroPago.setText("");
			sorter.setRowFilter(null);
			calcularTotalMes();
		});

		salvarPlanilhaBtn.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File("contas.xlsx"));
			int result = fileChooser.showSaveDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				try {
					File arquivo = fileChooser.getSelectedFile();
					List<Despesa> despesas = extrairDespesasDaTabela();
					excelGenerator.salvarDespesas(despesas, arquivo);
					JOptionPane.showMessageDialog(frame, "Planilha salva com sucesso!");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Erro ao salvar planilha: " + ex.getMessage());
				}
			}
		});

		abrirPlanilhaBtn.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			int result = fileChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				try {
					File arquivo = fileChooser.getSelectedFile();
					List<Despesa> despesas = excelGenerator.abrirDespesas(arquivo);
					despesaService.limparDespesas();
					modeloTabela.setRowCount(0);
					for (Despesa d : despesas) {
						despesaService.adicionarDespesa(d);
						NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
						nf.setMinimumFractionDigits(2);
						nf.setMaximumFractionDigits(2);

						modeloTabela.addRow(new Object[] {
							d.getConta(),
							d.isParcelada() ? "Sim" : "Não",
							d.getNumeroParcela(),
							nf.format(d.getValor()), // ✅ valor formatado corretamente
							d.isPago() ? "Sim" : "Não",
							d.getMes(),
							d.getAno(),
							d.getVencimento()
						});
					}
					calcularTotalMes();
					JOptionPane.showMessageDialog(frame, "Planilha carregada com sucesso!");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Erro ao abrir planilha: " + ex.getMessage());
				}
			}
		});

		adicionarBtn.addActionListener(e -> mostrarDialogAdicionar());

		editarBtn.addActionListener(e -> {
			int selectedRow = tabela.getSelectedRow();
			if (selectedRow >= 0) {
				mostrarDialogEditar(selectedRow);
			} else {
				JOptionPane.showMessageDialog(frame, "Selecione uma despesa para editar.");
			}
		});

		excluirBtn.addActionListener(e -> {
			int selectedRow = tabela.getSelectedRow();
			if (selectedRow >= 0) {
				int resposta = JOptionPane.showConfirmDialog(frame, "Deseja realmente excluir esta despesa?",
						"Confirmação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if (resposta == JOptionPane.YES_OPTION) {
					int modelIndex = tabela.convertRowIndexToModel(selectedRow);
					modeloTabela.removeRow(modelIndex);
					despesaService.removerDespesa(modelIndex);
					calcularTotalMes();
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Selecione uma despesa para excluir.");
			}
		});
		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filtros.add(new JLabel("Filtrar Mês:"));
		filtros.add(filtroMes);
		filtros.add(new JLabel("Pago:"));
		filtros.add(filtroPago);
		filtros.add(aplicarFiltroBtn);
		filtros.add(totalLabel);
		filtros.add(limparFiltroBtn);

		JPanel botoes = new JPanel();
		botoes.add(adicionarBtn);
		botoes.add(editarBtn);
		botoes.add(excluirBtn);
		botoes.add(abrirPlanilhaBtn);
		botoes.add(salvarPlanilhaBtn);

		frame.getContentPane().add(scrollTabela, BorderLayout.CENTER);
		frame.getContentPane().add(filtros, BorderLayout.NORTH);
		frame.getContentPane().add(botoes, BorderLayout.SOUTH);

		frame.setVisible(true);
	}

	private void mostrarDialogAdicionar() {
		JDialog dialog = new JDialog(frame, "Adicionar Despesa", true);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
		dialog.setSize(500, 400);
		dialog.setLocationRelativeTo(frame);

		JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField campoConta = new JTextField(30);
		linha1.add(new JLabel("Conta:"));
		linha1.add(campoConta);

		JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] meses = { "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro",
				"Outubro", "Novembro", "Dezembro" };
		JComboBox<String> comboMes = new JComboBox<>(meses);
		JComboBox<String> comboAno = new JComboBox<>();
		for (int i = 2000; i <= 2050; i++)
			comboAno.addItem(String.valueOf(i));
		linha2.add(new JLabel("Mês:"));
		linha2.add(comboMes);
		linha2.add(new JLabel("Ano:"));
		linha2.add(comboAno);

		JPanel linha3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox campoParcelada = new JCheckBox("Parcelada?");
		JTextField campoParcela = new JTextField(10);
		campoParcela.setEnabled(false);
		campoParcelada.addItemListener(e -> campoParcela.setEnabled(campoParcelada.isSelected()));
		linha3.add(campoParcelada);
		linha3.add(new JLabel("Nº Parcela:"));
		linha3.add(campoParcela);

		JPanel linha4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField campoValor = new JTextField(15);
		linha4.add(new JLabel("Valor:"));
		linha4.add(campoValor);

		JPanel linha5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JComboBox<String> comboDiaVenc = new JComboBox<>();
		for (int i = 1; i <= 31; i++)
			comboDiaVenc.addItem(String.format("%02d", i));
		JComboBox<String> comboMesVenc = new JComboBox<>(meses);
		JComboBox<String> comboAnoVenc = new JComboBox<>();
		for (int i = 2000; i <= 2050; i++)
			comboAnoVenc.addItem(String.valueOf(i));
		linha5.add(new JLabel("Vencimento:"));
		linha5.add(comboDiaVenc);
		linha5.add(comboMesVenc);
		linha5.add(comboAnoVenc);

		JPanel linha6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox campoPago = new JCheckBox("Pago");
		linha6.add(campoPago);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton salvarBtn = new JButton("Salvar");
		botoes.add(salvarBtn);

		salvarBtn.addActionListener(e -> {
			if (!camposObrigatoriosPreenchidos(campoConta.getText(), campoValor.getText(),
					comboMes.getSelectedItem().toString(), comboAno.getSelectedItem().toString(),
					comboDiaVenc.getSelectedItem().toString(), comboMesVenc.getSelectedItem().toString(),
					comboAnoVenc.getSelectedItem().toString())) {
				JOptionPane.showMessageDialog(dialog, "Preencha todos os campos obrigatórios.");
				return;
			}
			try {
				String valorTexto = campoValor.getText().replace(".", "").replace(",", ".");
				double valor = Double.parseDouble(valorTexto);

				String vencimento = comboDiaVenc.getSelectedItem() + " de " + comboMesVenc.getSelectedItem() + " de "
						+ comboAnoVenc.getSelectedItem();

				Despesa d = new Despesa(campoConta.getText(), campoParcelada.isSelected(),
						campoParcelada.isSelected() ? campoParcela.getText() : "", valor, campoPago.isSelected(),
						comboMes.getSelectedItem().toString(), comboAno.getSelectedItem().toString(), vencimento);

				despesaService.adicionarDespesa(d);

				NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
				nf.setMinimumFractionDigits(2);
				nf.setMaximumFractionDigits(2);

				modeloTabela.addRow(new Object[] { d.getConta(), d.isParcelada() ? "Sim" : "Não", d.getNumeroParcela(),
						nf.format(d.getValor()), d.isPago() ? "Sim" : "Não", d.getMes(), d.getAno(),
						d.getVencimento() });

				calcularTotalMes();
				dialog.dispose();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(dialog, "Erro: " + ex.getMessage());
			}
		});

		dialog.add(linha1);
		dialog.add(linha2);
		dialog.add(linha3);
		dialog.add(linha4);
		dialog.add(linha5);
		dialog.add(linha6);
		dialog.add(botoes);

		dialog.setVisible(true);
	}

	private List<Despesa> extrairDespesasDaTabela() {
		List<Despesa> lista = new ArrayList<>();
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));

		for (int i = 0; i < modeloTabela.getRowCount(); i++) {
			try {
				String conta = modeloTabela.getValueAt(i, 0).toString();
				boolean parcelada = modeloTabela.getValueAt(i, 1).toString().equalsIgnoreCase("Sim");
				String parcela = modeloTabela.getValueAt(i, 2).toString();
				String valorTexto = modeloTabela.getValueAt(i, 3).toString();

				// ✅ Verifica se o valor está em formato brasileiro
				double valor = nf.parse(valorTexto).doubleValue();

				boolean pago = modeloTabela.getValueAt(i, 4).toString().equalsIgnoreCase("Sim");
				String mes = modeloTabela.getValueAt(i, 5).toString();
				String ano = modeloTabela.getValueAt(i, 6).toString();
				String vencimento = modeloTabela.getValueAt(i, 7).toString();

				lista.add(new Despesa(conta, parcelada, parcela, valor, pago, mes, ano, vencimento));
			} catch (Exception e) {
				System.err.println("Erro ao extrair linha " + i + ": " + e.getMessage());
			}
		}
		return lista;
	}

	private void mostrarDialogEditar(int rowIndex) {
		int modelIndex = tabela.convertRowIndexToModel(rowIndex);
		if (modelIndex < 0)
			return;

		JDialog dialog = new JDialog(frame, "Editar Despesa", true);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
		dialog.setSize(500, 400);
		dialog.setLocationRelativeTo(frame);

		JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField campoConta = new JTextField(modeloTabela.getValueAt(modelIndex, 0).toString(), 30);
		linha1.add(new JLabel("Conta:"));
		linha1.add(campoConta);

		JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] meses = { "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro",
				"Outubro", "Novembro", "Dezembro" };
		JComboBox<String> comboMes = new JComboBox<>(meses);
		comboMes.setSelectedItem(modeloTabela.getValueAt(modelIndex, 5).toString());
		JComboBox<String> comboAno = new JComboBox<>();
		for (int i = 2000; i <= 2050; i++)
			comboAno.addItem(String.valueOf(i));
		comboAno.setSelectedItem(modeloTabela.getValueAt(modelIndex, 6).toString());
		linha2.add(new JLabel("Mês:"));
		linha2.add(comboMes);
		linha2.add(new JLabel("Ano:"));
		linha2.add(comboAno);

		JPanel linha3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox campoParcelada = new JCheckBox("Parcelada?");
		campoParcelada.setSelected(modeloTabela.getValueAt(modelIndex, 1).toString().equalsIgnoreCase("Sim"));
		JTextField campoParcela = new JTextField(modeloTabela.getValueAt(modelIndex, 2).toString(), 10);
		campoParcela.setEnabled(campoParcelada.isSelected());
		campoParcelada.addItemListener(e -> campoParcela.setEnabled(campoParcelada.isSelected()));
		linha3.add(campoParcelada);
		linha3.add(new JLabel("Nº Parcela:"));
		linha3.add(campoParcela);

		JPanel linha4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField campoValor = new JTextField(modeloTabela.getValueAt(modelIndex, 3).toString(), 15);
		linha4.add(new JLabel("Valor:"));
		linha4.add(campoValor);

		JPanel linha5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JComboBox<String> comboDiaVenc = new JComboBox<>();
		for (int i = 1; i <= 31; i++)
			comboDiaVenc.addItem(String.format("%02d", i));
		JComboBox<String> comboMesVenc = new JComboBox<>(meses);
		JComboBox<String> comboAnoVenc = new JComboBox<>();
		for (int i = 2000; i <= 2050; i++)
			comboAnoVenc.addItem(String.valueOf(i));

		String vencimento = modeloTabela.getValueAt(modelIndex, 7).toString();
		String[] partes = vencimento.split(" de ");
		if (partes.length == 3) {
			comboDiaVenc.setSelectedItem(partes[0]);
			comboMesVenc.setSelectedItem(partes[1]);
			comboAnoVenc.setSelectedItem(partes[2]);
		}

		linha5.add(new JLabel("Vencimento:"));
		linha5.add(comboDiaVenc);
		linha5.add(comboMesVenc);
		linha5.add(comboAnoVenc);

		JPanel linha6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox campoPago = new JCheckBox("Pago");
		campoPago.setSelected(modeloTabela.getValueAt(modelIndex, 4).toString().equalsIgnoreCase("Sim"));
		linha6.add(campoPago);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton salvarBtn = new JButton("Salvar Alterações");
		botoes.add(salvarBtn);

		salvarBtn.addActionListener(e -> {
			if (!camposObrigatoriosPreenchidos(campoConta.getText(), campoValor.getText(),
					comboMes.getSelectedItem().toString(), comboAno.getSelectedItem().toString(),
					comboDiaVenc.getSelectedItem().toString(), comboMesVenc.getSelectedItem().toString(),
					comboAnoVenc.getSelectedItem().toString())) {
				JOptionPane.showMessageDialog(dialog, "Preencha todos os campos obrigatórios.");
				return;
			}
			try {
				String valorTexto = campoValor.getText().replace(".", "").replace(",", ".");
				double valor = Double.parseDouble(valorTexto);

				String novoVencimento = comboDiaVenc.getSelectedItem() + " de " + comboMesVenc.getSelectedItem()
						+ " de " + comboAnoVenc.getSelectedItem();

				Despesa d = new Despesa(campoConta.getText(), campoParcelada.isSelected(),
						campoParcelada.isSelected() ? campoParcela.getText() : "", valor, campoPago.isSelected(),
						comboMes.getSelectedItem().toString(), comboAno.getSelectedItem().toString(), novoVencimento);

				despesaService.atualizarDespesa(modelIndex, d);

				NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
				nf.setMinimumFractionDigits(2);
				nf.setMaximumFractionDigits(2);

				modeloTabela.setValueAt(d.getConta(), modelIndex, 0);
				modeloTabela.setValueAt(d.isParcelada() ? "Sim" : "Não", modelIndex, 1);
				modeloTabela.setValueAt(d.getNumeroParcela(), modelIndex, 2);
				modeloTabela.setValueAt(nf.format(d.getValor()), modelIndex, 3);
				modeloTabela.setValueAt(d.isPago() ? "Sim" : "Não", modelIndex, 4);
				modeloTabela.setValueAt(d.getMes(), modelIndex, 5);
				modeloTabela.setValueAt(d.getAno(), modelIndex, 6);
				modeloTabela.setValueAt(d.getVencimento(), modelIndex, 7);

				calcularTotalMes();
				dialog.dispose();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(dialog, "Erro: " + ex.getMessage());
			}
		});

		dialog.add(linha1);
		dialog.add(linha2);
		dialog.add(linha3);
		dialog.add(linha4);
		dialog.add(linha5);
		dialog.add(linha6);
		dialog.add(botoes);

		dialog.setVisible(true);
	}

	private void calcularTotalMes() {
		double total = 0.0;
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));

		for (int i = 0; i < tabela.getRowCount(); i++) {
			int modelIndex = tabela.convertRowIndexToModel(i);
			Object valorObj = modeloTabela.getValueAt(modelIndex, 3);
			if (valorObj != null) {
				try {
					double valor = nf.parse(valorObj.toString()).doubleValue();
					total += valor;
				} catch (Exception e) {
					System.err.println("Erro ao somar valor: " + e.getMessage());
				}
			}
		}
		totalLabel.setText(String.format("Total do mês: R$ %.2f", total));
	}

	private boolean camposObrigatoriosPreenchidos(String conta, String valor, String mes, String ano, String diaVenc,
			String mesVenc, String anoVenc) {
		return !conta.trim().isEmpty() && !valor.trim().isEmpty() && !mes.trim().isEmpty() && !ano.trim().isEmpty()
				&& !diaVenc.trim().isEmpty() && !mesVenc.trim().isEmpty() && !anoVenc.trim().isEmpty();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(GastosSwingApp::new);
	}
}