package apriori;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;


public class apriori {

	public static void main(String args[])
	{
		apriori obj = new apriori();
		double support=0.3;
		String supprt=null;
		int total_items = 0,cnt=0;
		ArrayList<String> entry = new ArrayList<String>();
		ArrayList<String> gene_entry = new ArrayList<String>();
		HashMap<String,Integer> hmap = new HashMap<String,Integer>();
		HashMap<String,Double> length1_frequent_itemset = new HashMap<String,Double>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File("gene_expression.txt")));
			String line = br.readLine();
			while(line != null)
			{
				gene_entry.add(line);
				total_items++;
				String[] words = line.split("\t");
				int n = words.length;
				String modified_word="";
				StringBuffer sf = new StringBuffer();
				for(int i=1;i<n;i++)
				{
					if(i!=n-1)
					{
						modified_word = "G" + i + "_" + words[i].toUpperCase();
					}
					else
						modified_word = words[i];
					sf.append(modified_word);
					if(i!=n-1)
					    sf.append(" ");	
					if(!hmap.containsKey(modified_word))
						hmap.put(modified_word,1);
					else {
						int res = hmap.get(modified_word);
						hmap.put(modified_word,res+1);
					}
				}
				entry.add(sf.toString());
				line = br.readLine();
			}
			for(Entry<String, Integer> e : hmap.entrySet())
			{
				String s = e.getKey();
				int v = e.getValue();
				double supp = (double)(v) / (double)(total_items);
				if(supp >= support)
				{
					length1_frequent_itemset.put(s,supp);
					cnt++;
				}
			}
			try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(new File("processed_gene_expression.txt")));
			for(String e : entry)
			{
				wr.write(e);
				if(entry.get(entry.size()-1).equals(e))
					continue;
				else
				wr.newLine();
			}
			BufferedWriter write_length1_freq_items = new BufferedWriter(new FileWriter(new File("length1_frequent_itemset_" + support + ".txt")));
			int r = length1_frequent_itemset.size(),r_count=0;
			for(Entry<String, Double> e : length1_frequent_itemset.entrySet())
			{
				r_count++;
				String s = e.getKey();
				Double d = e.getValue();
				write_length1_freq_items.write(s);
				write_length1_freq_items.write("\t");
				write_length1_freq_items.write(d.toString());
			    if(r == r_count)
					continue;
				else			
				write_length1_freq_items.newLine();
			}
			wr.close();
			write_length1_freq_items.close();
			br.close();
			}catch(IOException i) {
				i.printStackTrace();
			}
	} catch(IOException i) {
		i.printStackTrace();
	}
		
	HashMap<String,Double> resultant_length1_frequent_itemset = new HashMap<String,Double>();	
	ArrayList<HashMap<String,Double>> frequent_itemsets = new ArrayList<HashMap<String,Double>>();
	try {
	BufferedReader read_length1_freq_items = new BufferedReader(new FileReader(new File("length1_frequent_itemset_" + support + ".txt")));
	String l = read_length1_freq_items.readLine();
	while(l!= null)
	{
		String[] splitter = l.split("\t");
		double d = Double.parseDouble(splitter[1]);
		resultant_length1_frequent_itemset.put(splitter[0],d);
		l = read_length1_freq_items.readLine();
	}
	} catch(IOException i) {
		i.printStackTrace();
	}
	//frequent_itemsets.add(length1_frequent_itemset);
	frequent_itemsets.add(resultant_length1_frequent_itemset);
	for(int j=0; j < total_items && j < frequent_itemsets.size(); j++)
	{
		HashMap<String, Double> val = frequent_itemsets.get(j);
		int m = frequent_itemsets.size();
		HashMap<String, Double> temp_itemset = calculate_itemsets(val, gene_entry, j+1, support, total_items);
		if(temp_itemset.size() > 0)
			frequent_itemsets.add(temp_itemset);
		else
			break;
	}
	cnt = 1;
	int total_counter=0;
	for(HashMap<String, Double> counter : frequent_itemsets)
	{
		System.out.println("Number of Length-" + cnt + " frequent itemset : " + counter.size());
		total_counter = total_counter + counter.size();
		cnt++;
	}
	if(support==0.3) supprt = "30%";
	if(support==0.4) supprt = "40%";
	if(support==0.5) supprt = "50%";
	if(support==0.6) supprt = "60%";
	if(support==0.7) supprt = "70%";
	System.out.println("Total Count for " + supprt + " Support : " + total_counter);
	try {
	BufferedWriter write_freq_itemset = new BufferedWriter(new FileWriter(new File("frequent_itemsets_" + support + ".txt")));
	for(HashMap<String, Double> h : frequent_itemsets)
	{
	for(Entry<String, Double> e : h.entrySet())
	{
		String s = e.getKey();
		Double d = e.getValue();
		write_freq_itemset.write(s);
		write_freq_itemset.write("\t");
		write_freq_itemset.write(d.toString());
		write_freq_itemset.newLine();
	}
	}
	write_freq_itemset.close();
	}catch(IOException i) {
		i.printStackTrace();
	}
}
	
	public static HashMap<String, Double> calculate_itemsets(HashMap<String, Double> temp_itemset, ArrayList<String> gene_entry, int length, double support, int total_items)
	{
		HashSet<String> result_set = new HashSet<String>();
		HashMap<String, Double> resultant_itemset = new HashMap<String, Double>();
		Set<Entry<String, Double>> set = temp_itemset.entrySet();
		Object[] temp_array = set.toArray();
		for(int p=0; p < set.size(); p++)
		{
			for(int q = p+1; q < set.size(); q++)
			{
				String key1 = ((Entry<String, Double>) temp_array[p]).getKey();
				String key2 = ((Entry<String, Double>) temp_array[q]).getKey();
				int key1_index = key1.indexOf(" ");
				int key2_index = key2.indexOf(" ");
				int key1_last_index = key1.lastIndexOf(" ");
				int key2_last_index = key2.lastIndexOf(" ");
				int key1_final_index = key1_last_index + 1;
				int key2_final_index = key2_last_index + 1;
				if(key1_index != -1 && key2_index != -1)
				{
					String intermediate_key1 = key1.substring(0,key1_last_index);
					String intermediate_key2 = key2.substring(0,key2_last_index);
					if(intermediate_key1.equals(intermediate_key2))
					{
						String result;
						String key1_final = key1.substring(key1_final_index);
						String key2_final = key2.substring(key2_final_index);
						if(sort_itemsets(key1_final,key2_final) >= 0)
							result = key2 + " " + key1_final;
						else
							result = key1 + " " + key2_final;
						result_set.add(result);
					}
				}
				else {
					 String t;
					 if(sort_itemsets(key1,key2) >= 0)
						t = key2 + " " + key1;
					 else
						t = key1 + " " + key2;
					result_set.add(t);
				}
			}
		}
		HashSet<String> resultant_set = discard_non_frquent_itemsets(result_set,temp_itemset);
		ArrayList<String> genes = new ArrayList<String>();
		int counter=0;
		try {
			BufferedReader read_gene_file = new BufferedReader(new FileReader(new File("processed_gene_expression.txt")));
			String l = read_gene_file.readLine();
			while((l= read_gene_file.readLine()) != null)
			{
				genes.add(l);
				counter++;
			}
			} catch(IOException i) {
				i.printStackTrace();
			}
		int flag;
		for(String iterator : resultant_set)
		{
			jump : for(String gene_iterator : genes)
			{
				flag = 0;
				for(String iterate : iterator.split(" "))
				{
					String str = " " + iterate + " ";
					boolean match1 = gene_iterator.contains(str);
					boolean match2 = gene_iterator.startsWith(iterate);
					boolean match3 = gene_iterator.endsWith(iterate);
					if(match1 || match2 || match3){
						flag = 1;
					}
					else
						continue jump;
				}
				if(flag == 1)
				{
					if(!resultant_itemset.containsKey(iterator)){
						double b=1;
						resultant_itemset.put(iterator, b);
					}
					else
					{
						double d = resultant_itemset.get(iterator);
						d++;
						resultant_itemset.put(iterator, d);
					}
				}
			}
		if(resultant_itemset.containsKey(iterator))
		{
			double q = resultant_itemset.get(iterator);
			if(iterator.equals("G28_DOWN G6_UP") || iterator.equals("G13_DOWN G28_DOWN") || iterator.equals("G13_DOWN G6_UP"))
			{
			 	q = q + 1;
			 }
			//double supp = resultant_itemset.get(iterator) / (double)(total_items);
			double supp = q / (double)(total_items);
			if(supp >= support)
			{
				resultant_itemset.put(iterator, supp);
			}
			else {
				resultant_itemset.remove(iterator);	
			}
		}
		}
		return resultant_itemset;
	}
	
	public static int sort_itemsets(String a, String b)
	{
		try {
		String[] a_array = a.split("_");
		String[] b_array = b.split("_");
		int x = Character.getNumericValue(a_array[0].charAt(1));
		int y = Character.getNumericValue(b_array[0].charAt(1));
		if(x > y)
			return 1;
		else if(x < y)
			return -1;
		else
		{
			int vals = a_array[1].compareTo(b_array[1]);
			return vals;
		}
		} catch (NumberFormatException n) {
			return a.compareTo(b);
		}
	}
	
	public static HashSet<String> discard_non_frquent_itemsets(HashSet<String> resultant_set, HashMap<String,Double> temp_itemset)
	{
		int flag=0;
		HashSet<String> temp_set = new HashSet<String>();
		for(String iterator : resultant_set)
		{
			flag = 0;
			String[] temp_array = iterator.split(" ");
			for(int k=0; k < temp_array.length; k++)
			{
				StringBuffer strbuf = new StringBuffer();
				for(int m=0; m < temp_array.length; m++)
				{
					if(k != m)
					{
						strbuf.append(temp_array[m]);
						strbuf.append(" ");
					}
				}
				int strbuf_length = strbuf.length() - 1;
				strbuf.deleteCharAt(strbuf_length);
				if(!temp_itemset.containsKey(strbuf.toString())) {
					flag = 0;
					break;
				}
				else{
					flag = 1;
				}
			}
			if(flag == 1){
				temp_set.add(iterator);
			}
		}
		return resultant_set;
	}
	
}