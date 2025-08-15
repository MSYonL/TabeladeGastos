package br.com.projeto.tabelagastos.service;

import br.com.projeto.tabelagastos.model.Despesa;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExcelGenerator {

	private static final String[] COLUNAS = { "Conta", "Parcelada", "Parcela", "Valor", "Pago", "Mês", "Ano",
			"Vencimento" };

	// Salva lista de despesas em arquivo Excel
	public void salvarDespesas(List<Despesa> despesas, File arquivo) throws IOException {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Contas");

			// 🎨 Estilo do cabeçalho
			CellStyle estiloCabecalho = workbook.createCellStyle();
			Font fonteCabecalho = workbook.createFont();
			fonteCabecalho.setBold(true);
			estiloCabecalho.setFont(fonteCabecalho);
			estiloCabecalho.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			fonteCabecalho.setColor(IndexedColors.WHITE.getIndex());
			estiloCabecalho.setAlignment(HorizontalAlignment.CENTER);
			estiloCabecalho.setBorderBottom(BorderStyle.THIN);
			estiloCabecalho.setBorderTop(BorderStyle.THIN);
			estiloCabecalho.setBorderLeft(BorderStyle.THIN);
			estiloCabecalho.setBorderRight(BorderStyle.THIN);

			// 💰 Estilo para valores monetários
            CellStyle estiloValor = workbook.createCellStyle();
            DataFormat formato = workbook.createDataFormat();
            estiloValor.setDataFormat(formato.getFormat("\"R$\" #,##0.00"));
            estiloValor.setAlignment(HorizontalAlignment.RIGHT);
            estiloValor.setBorderBottom(BorderStyle.THIN);
            estiloValor.setBorderTop(BorderStyle.THIN);
            estiloValor.setBorderLeft(BorderStyle.THIN);
            estiloValor.setBorderRight(BorderStyle.THIN);
            

			// 🧾 Estilo padrão para células
			CellStyle estiloPadrao = workbook.createCellStyle();
			estiloPadrao.setAlignment(HorizontalAlignment.CENTER);
			estiloPadrao.setBorderBottom(BorderStyle.THIN);
			estiloPadrao.setBorderTop(BorderStyle.THIN);
			estiloPadrao.setBorderLeft(BorderStyle.THIN);
			estiloPadrao.setBorderRight(BorderStyle.THIN);

			// Cabeçalho
			// Cabeçalho com estilo profissional
			Row header = sheet.createRow(0);
			for (int i = 0; i < COLUNAS.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(COLUNAS[i]);
				cell.setCellStyle(estiloCabecalho);
				sheet.autoSizeColumn(i);
			}

			// Filtro e congelamento
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, COLUNAS.length - 1));
			sheet.createFreezePane(0, 1);

			// Dados
			for (int i = 0; i < despesas.size(); i++) {
				Despesa d = despesas.get(i);
				Row row = sheet.createRow(i + 1);

				Cell cell0 = row.createCell(0);
				cell0.setCellValue(d.getConta());
				cell0.setCellStyle(estiloPadrao);

				Cell cell1 = row.createCell(1);
				cell1.setCellValue(d.isParcelada() ? "Sim" : "Não");
				cell1.setCellStyle(estiloPadrao);

				Cell cell2 = row.createCell(2);
				cell2.setCellValue(d.getNumeroParcela());
				cell2.setCellStyle(estiloPadrao);

				Cell cell3 = row.createCell(3);
				cell3.setCellValue(d.getValor());
				cell3.setCellStyle(estiloValor); // 💰 moeda

				Cell cell4 = row.createCell(4);
				cell4.setCellValue(d.isPago() ? "Sim" : "Não");
				cell4.setCellStyle(estiloPadrao);

				Cell cell5 = row.createCell(5);
				cell5.setCellValue(d.getMes());
				cell5.setCellStyle(estiloPadrao);

				Cell cell6 = row.createCell(6);
				cell6.setCellValue(d.getAno());
				cell6.setCellStyle(estiloPadrao);

				Cell cell7 = row.createCell(7);
				cell7.setCellValue(d.getVencimento());
				cell7.setCellStyle(estiloPadrao);
			}

			// 📏 Ajusta largura das colunas
			for (int i = 0; i < COLUNAS.length; i++) {
				sheet.autoSizeColumn(i);
			}
			
			
			// 📊 Linha de totalizador com fórmula dinâmica
			Row totalRow = sheet.createRow(despesas.size() + 1);

			Cell totalLabel = totalRow.createCell(2);
			totalLabel.setCellValue("Total:");
			totalLabel.setCellStyle(estiloCabecalho); // opcional: destaque

			Cell totalValue = totalRow.createCell(3);
			String formula = String.format("SUBTOTAL(109,D2:D%d)", despesas.size() + 1);
			totalValue.setCellFormula(formula);
			totalValue.setCellStyle(estiloValor);;

			// Salva no disco
			try (FileOutputStream fos = new FileOutputStream(arquivo)) {
				workbook.write(fos);
			}
		}
	}

	// Lê despesas de um arquivo Excel
	public List<Despesa> abrirDespesas(File arquivo) throws IOException {
		List<Despesa> despesas = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(arquivo)) {
			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i < sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				String conta = getCellValue(row.getCell(0));
				boolean parcelada = interpretarBoolean(getCellValue(row.getCell(1)));
				String parcela = getCellValue(row.getCell(2));
				double valor = interpretarDouble(getCellValue(row.getCell(3)));
				boolean pago = interpretarBoolean(getCellValue(row.getCell(4)));
				String mes = getCellValue(row.getCell(5));
				String ano = getCellValue(row.getCell(6));
				String vencimento = getCellValue(row.getCell(7));

				despesas.add(new Despesa(conta, parcelada, parcela, valor, pago, mes, ano, vencimento));
			}

		}

		return despesas;
	}

	// Interpreta valores booleanos
	private boolean interpretarBoolean(String valor) {
		return valor.equalsIgnoreCase("Sim") || valor.equalsIgnoreCase("true");
	}

	// Converte string para double, respeitando formato brasileiro
	private double interpretarDouble(String valor) {
		try {
			NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("pt-BR"));
			return nf.parse(valor).doubleValue();
		} catch (Exception e) {
			return 0.0;
		}
	}

	// Extrai valor da célula com tratamento de tipo
	private String getCellValue(Cell cell) {
		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
			} else {
				DecimalFormat df = new DecimalFormat("###,###.00",
						new DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")));
				return df.format(cell.getNumericCellValue());
			}
		case BOOLEAN:
			return cell.getBooleanCellValue() ? "true" : "false";
		case FORMULA:
			try {
				DecimalFormat df = new DecimalFormat("###,###.00",
						new DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")));
				return df.format(cell.getNumericCellValue());
			} catch (IllegalStateException e) {
				return String.valueOf(cell.getNumericCellValue());
			}
		default:
			return "";
		}
	}
}