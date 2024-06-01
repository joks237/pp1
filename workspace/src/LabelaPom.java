package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

public class LabelaPom {
	private String nazivLabele;
	private int adresaLabele;
	public ArrayList<Integer> gotoListaLabela = new ArrayList<Integer>();
	
	public LabelaPom(String name, int adress) {
		nazivLabele = name;
		adresaLabele = adress;
	}
	
	public String getNazivLabele() {
		return nazivLabele;
	}
	
	public int getAdresaLabele() {
		return adresaLabele;
	}
}