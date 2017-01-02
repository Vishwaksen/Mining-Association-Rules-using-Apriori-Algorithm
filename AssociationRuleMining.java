import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vishwaksen
 *
 */
public class AssociationRuleMining {

	/**
	 * @param args
	 */

	private static final String prefix = "B:\\fall16\\601\\hw2\\submitted\\";
	private static final String fileName = "frequent_itemsets_0.5.txt";
	private static final int itemSetSize = 65;
	private static final Map<String, Double> itemset = new HashMap<>();
	private static final Pattern pattern = Pattern.compile("\\d+");
	private static int[][] itemSetCols = new int[itemSetSize][];
	private static Map<Integer, Double> support = new HashMap<>();

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(prefix + fileName)));
		String line = null;
		Map<Integer, String> itemsetMappedToIndex = new HashMap<>();
		int itemSetCounter = 0;
		Matcher matcher = null;
		while ((line = br.readLine()) != null) {
			String[] splitInput = line.split("\t");
			String itemSet = splitInput[0];
			double supportVal = Double.parseDouble(splitInput[1]);
			String[] splitItemSet = itemSet.split(" ");
			itemset.put(itemSet, supportVal);
			if (splitItemSet.length < 2) {
				continue; // we need atleast 2 items in the itemset for rule
							// geenration
			}
			itemsetMappedToIndex.put(itemSetCounter, itemSet);
			int[] splitItemSetCol = new int[splitItemSet.length];
			for (int i = 0; i < splitItemSet.length; i++) {
				matcher = pattern.matcher(splitItemSet[i]);
				if (matcher.find()) {
					splitItemSetCol[i] = Integer.valueOf(matcher.group());
				} else {
					splitItemSetCol[i] = 101;
				}
			}
			support.put(itemSetCounter, supportVal);
			itemSetCols[itemSetCounter++] = splitItemSetCol;
		}

		Map<Integer, int[]> bits = new HashMap<>();
		for (int i = 0; i < itemSetCols.length; i++) {
			int bit4 = 0, bit3 = 0, bit2 = 0, bit1 = 0;
			for (int eachValue : itemSetCols[i]) {
				if (eachValue >= 1 && eachValue <= 31) {
					bit1 |= 1 << eachValue;
				} else if (eachValue >= 32 && eachValue <= 63) {
					bit2 |= 1 << (eachValue % 32);
				} else if (eachValue >= 64 && eachValue <= 95) {
					bit3 |= 1 << (eachValue % 64);
				} else {
					bit4 |= 1 << (eachValue % 96);
				}
				bits.put(i, new int[] { bit4, bit3, bit2, bit1 });
			}
		}

		// Query 1 - RULE HAS ANY OF G6_UP
		System.out.println("\nRULE HAS ANY OF G6_UP");
		String query = "G6_UP";
		Set<String> associationRules = fetchRules(bits, itemsetMappedToIndex, query, null);
		printRules(associationRules);

		// 2. RULE HAS 1 OF G1_UP
		System.out.println("\nRULE HAS 1 OF G1_UP");
		query = "G1_UP";
		associationRules = fetchRules(bits, itemsetMappedToIndex, query, null);
		printRules(associationRules);

		// 3. RULE HAS 1 OF (G1_UP, G10_DOWN)
		System.out.println("\nRULE HAS 1 OF (G1_UP, G10_DOWN)");
		String multiquery = "G1_UP,G10_DOWN";
		query = "G10_DOWN G1_UP";
		associationRules = fetchRules(bits, itemsetMappedToIndex, query, multiquery);
		printRules(associationRules);

		// 4. BODY HAS ANY OF G6_UP
		System.out.println("\nBODY HAS ANY OF G6_UP");
		printRules(bodySingle(itemsetMappedToIndex, "G6_UP"));

		// 5. BODY HAS NONE OF G72_UP
		System.out.println("\nBODY HAS NONE OF G72_UP");
		printRules(bodyNoneSingle(itemsetMappedToIndex, "G72_UP"));

		// 6. BODY HAS 1 OF (G1_UP, G10_DOWN)
		System.out.println("\nBODY HAS 1 OF (G1_UP, G10_DOWN)");
		printRules(bodyDouble(itemsetMappedToIndex, "G1_UP,G10_DOWN"));

		// 7. HEAD HAS ANY OF G6_UP
		System.out.println("\nHEAD HAS ANY OF G6_UP");
		printRules(headSingle(itemsetMappedToIndex, "G6_UP"));

		// 8. HEAD HAS NONE OF (G1_UP, G6_UP)
		System.out.println("\nHEAD HAS NONE OF (G1_UP, G6_UP)");
		headNotDouble(itemsetMappedToIndex, "G1_UP,G6_UP");

		// 9. HEAD HAS 1 OF (G6_UP, G8_UP)
		System.out.println("\nHEAD HAS 1 OF (G6_UP, G8_UP)");
		headDouble(itemsetMappedToIndex, "G6_UP,G8_UP");

		// 10. RULE HAS 1 OF (G1_UP, G6_UP, G72_UP)
		System.out.println("\nRULE HAS 1 OF (G1_UP, G6_UP, G72_UP)");
		multiquery = "G1_UP,G6_UP,G72_UP";
		query = "G1_UP G6_UP G72_UP";
		associationRules = fetchRules(bits, itemsetMappedToIndex, query, multiquery);
		printRules(associationRules);

		// 11. RULE HAS ANY OF (G1_UP, G6_UP, G72_UP)
		System.out.println("\nRULE HAS ANY OF (G1_UP, G6_UP, G72_UP)");
		query = "G1_UP";
		associationRules = fetchRules(bits, itemsetMappedToIndex, query, null);
		query = "G6_UP";
		associationRules.addAll(fetchRules(bits, itemsetMappedToIndex, query, null));
		query = "G72_UP";
		associationRules.addAll(fetchRules(bits, itemsetMappedToIndex, query, null));
		// since this is a set, we don't have to worry about items containing
		// both G1_UP and G10_DOWN getting repeated
		printRules(associationRules);

		// For template 2:
		// 1. SIZE OF RULE >= 3
		System.out.println("\nSIZE OF RULE >= 3");
		printRules(template2Rule(itemsetMappedToIndex));

		// 2. SIZE OF BODY >= 2
		System.out.println("\nSIZE OF BODY >= 2");
		template2body(itemsetMappedToIndex);

		// 3. SIZE OF HEAD >= 2
		System.out.println("\nSIZE OF HEAD >= 2");
		template2head(itemsetMappedToIndex);

		// For template 3:
		// 1. BODY HAS ANY OF G1_UP AND HEAD HAS 1 OF G59_UP
		System.out.println("\nBODY HAS ANY OF G1_UP AND HEAD HAS 1 OF G59_UP");
		Set<String> partialResult = bodySingle(itemsetMappedToIndex, "G1_UP");
		partialResult.retainAll(headSingle(itemsetMappedToIndex, "G59_UP"));
		printRules(partialResult);

		// 2. BODY HAS ANY OF G1_UP OR HEAD HAS 1 OF G6_UP
		System.out.println("\nBODY HAS ANY OF G1_UP OR HEAD HAS 1 OF G6_UP");
		partialResult = bodySingle(itemsetMappedToIndex, "G1_UP");
		partialResult.addAll(headSingle(itemsetMappedToIndex, "G6_UP"));
		printRules(partialResult);

		// 3. BODY HAS 1 OF G1_UP OR HEAD HAS 2 OF G6_UP
		System.out.println("\nBODY HAS 1 OF G1_UP OR HEAD HAS 2 OF G6_UP");
		partialResult = bodySingle(itemsetMappedToIndex, "G1_UP");
		// did not compute the second half as the HEAD will never have 2 G6_UP
		printRules(partialResult);

		// 4. HEAD HAS 1 OF G1_UP AND BODY HAS 0 OF DISEASE
		System.out.println("\nHEAD HAS 1 OF G1_UP AND BODY HAS 0 OF DISEASE");
		partialResult = headSingle(itemsetMappedToIndex, "G1_UP");
		partialResult.retainAll(bodyNoneSingle(itemsetMappedToIndex, "ALL,AML,BREAST CANCER,COLON CANCER"));
		printRules(partialResult);

		// 5. HEAD HAS 1 OF DISEASE OR RULE HAS 1 OF (G72_UP, G96_DOWN)
		System.out.println("\nHEAD HAS 1 OF DISEASE OR RULE HAS 1 OF (G72_UP, G96_DOWN)");
		multiquery = "G72_UP,G96_DOWN";
		query = "G72_UP G96_DOWN";
		associationRules = fetchRules(bits, itemsetMappedToIndex, query, multiquery);
		associationRules.addAll(headSingle(itemsetMappedToIndex, "ALL,AML,BREAST CANCER,COLON CANCER"));
		printRules(associationRules);

		// 6. BODY HAS 1 of (G59_UP, G96_DOWN) AND SIZE OF RULE >=3
		System.out.println("\nBODY HAS 1 of (G59_UP, G96_DOWN) AND SIZE OF RULE >=3");
		partialResult = template2Rule(itemsetMappedToIndex);
		partialResult.retainAll(bodyDouble(itemsetMappedToIndex, "G59_UP,G96_DOWN"));
		printRules(partialResult);

		br.close();
	}

	private static Set<String> fetchRules(Map<Integer, int[]> bits, Map<Integer, String> itemsetMappedToIndex,
			String query, String multiQuery) {
		Set<String> associationRules = new LinkedHashSet<>();
		if (multiQuery == null) {
			Matcher matcher = pattern.matcher(query);
			int input_term;
			if (matcher.find()) {
				input_term = Integer.valueOf(matcher.group());
			} else {
				input_term = 101;
			}

			int input_term_bits = 1 << input_term;
			Iterator<Map.Entry<Integer, int[]>> itr = bits.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, int[]> each = itr.next();
				if (input_term >= 1 && input_term <= 31) {
					if ((input_term_bits & each.getValue()[3]) == input_term_bits) {
						// System.out.println(Arrays.toString(itemSetCols[each.getKey()])
						// + " support: " + support.get(each.getKey()));
						// System.out.println(itemsetMappedToIndex.get(each.getKey()));
						if (itemsetMappedToIndex.get(each.getKey()).contains(query)) {
							associationRules.addAll(processRules(itemsetMappedToIndex.get(each.getKey())));
						}
					}
				} else if (input_term >= 32 && input_term <= 63) {
					if ((input_term_bits & each.getValue()[2]) == input_term_bits) {
						// System.out.println(Arrays.toString(itemSetCols[each.getKey()])
						// + " support: " + support.get(each.getKey()));
						// System.out.println(itemsetMappedToIndex.get(each.getKey()));
						if (itemsetMappedToIndex.get(each.getKey()).contains(query)) {
							associationRules.addAll(processRules(itemsetMappedToIndex.get(each.getKey())));
						}
					}
				} else if (input_term >= 64 && input_term <= 95) {
					if ((input_term_bits & each.getValue()[1]) == input_term_bits) {
						// System.out.println(Arrays.toString(itemSetCols[each.getKey()])
						// + " support: "
						// + support.get(each.getKey()));
						// System.out.println(itemsetMappedToIndex.get(each.getKey()));
						if (itemsetMappedToIndex.get(each.getKey()).contains(query)) {
							associationRules.addAll(processRules(itemsetMappedToIndex.get(each.getKey())));
						}
					}
				} else {
					if ((input_term_bits & each.getValue()[0]) == input_term_bits) {
						// System.out.println(Arrays.toString(itemSetCols[each.getKey()])
						// + " support: " + support.get(each.getKey()));
						// System.out.println(itemsetMappedToIndex.get(each.getKey()));
						if (itemsetMappedToIndex.get(each.getKey()).contains(query)) {
							associationRules.addAll(processRules(itemsetMappedToIndex.get(each.getKey())));
						}
					}
				}
			}
		} else {
			String[] querySplit = multiQuery.split(",");
			int bit4 = 0, bit3 = 0, bit2 = 0, bit1 = 0;
			for (String eachQuery : querySplit) {
				Matcher matcher = pattern.matcher(eachQuery);
				int input_term;
				if (matcher.find()) {
					input_term = Integer.valueOf(matcher.group());
				} else {
					input_term = 101;
				}
				if (input_term >= 1 && input_term <= 31) {
					bit1 |= 1 << input_term;
				} else if (input_term >= 32 && input_term <= 63) {
					bit2 |= 1 << (input_term % 32);
				} else if (input_term >= 64 && input_term <= 95) {
					bit3 |= 1 << (input_term % 64);
				} else {
					bit4 |= 1 << (input_term % 96);
				}
			}
			Iterator<Map.Entry<Integer, int[]>> itr = bits.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, int[]> each = itr.next();
				if (((bit1 & each.getValue()[3]) == bit1) && ((bit2 & each.getValue()[2]) == bit2)
						&& ((bit3 & each.getValue()[1]) == bit3) && ((bit4 & each.getValue()[0]) == bit4)) {
					if (itemsetMappedToIndex.get(each.getKey()).contains(query)) {
						associationRules.addAll(processRules(itemsetMappedToIndex.get(each.getKey())));
					}
				}
			}
		}
		return associationRules;
	}

	private static Collection<? extends String> processRules(String itemsetContents) {
		String[] itemsetSplit = itemsetContents.split(" ");
		List<String> rules = new ArrayList<>();
		if (itemsetSplit.length == 2) {
			double supportComb = itemset.get(itemsetContents);
			double support0 = itemset.get(itemsetSplit[0]);
			double support1 = itemset.get(itemsetSplit[1]);
			if (supportComb / support0 >= 0.6) {
				rules.add(itemsetSplit[0] + " --> " + itemsetSplit[1]);
			}
			if (supportComb / support1 >= 0.6) {
				rules.add(itemsetSplit[1] + " --> " + itemsetSplit[0]);
			}
		} else if (itemsetSplit.length == 3) {
			double supportComb = itemset.get(itemsetContents);
			double support0 = itemset.get(itemsetSplit[0]);
			double support1 = itemset.get(itemsetSplit[1]);
			double support2 = itemset.get(itemsetSplit[2]);
			double support3 = itemset.get(itemsetSplit[0] + " " + itemsetSplit[1]);
			double support4 = itemset.get(itemsetSplit[0] + " " + itemsetSplit[2]);
			double support5 = itemset.get(itemsetSplit[1] + " " + itemsetSplit[2]);
			if (supportComb / support0 >= 0.6) {
				rules.add(itemsetSplit[0] + " --> {" + itemsetSplit[1] + ", " + itemsetSplit[2] + "}");
			}
			if (supportComb / support1 >= 0.6) {
				rules.add(itemsetSplit[1] + " --> {" + itemsetSplit[0] + ", " + itemsetSplit[2] + "}");
			}
			if (supportComb / support2 >= 0.6) {
				rules.add(itemsetSplit[2] + " --> {" + itemsetSplit[0] + ", " + itemsetSplit[1] + "}");
			}
			if (supportComb / support3 >= 0.6) {
				rules.add("{" + itemsetSplit[0] + ", " + itemsetSplit[1] + "} --> " + itemsetSplit[2]);
			}
			if (supportComb / support4 >= 0.6) {
				rules.add("{" + itemsetSplit[0] + ", " + itemsetSplit[2] + "} --> " + itemsetSplit[1]);
			}
			if (supportComb / support5 >= 0.6) {
				rules.add("{" + itemsetSplit[1] + ", " + itemsetSplit[2] + "} --> " + itemsetSplit[0]);
			}
		} else {
			// currently we don't have length3+ item sets
		}
		return rules;
	}

	private static void printRules(Set<String> associationRules) {
		// printing the association rules
		if (associationRules == null || associationRules.size() == 0) {
			System.out.println("No rules generated!");
		} else {
			System.out.println("Count: " + associationRules.size());
			for (String s : associationRules) {
				System.out.println(s);
			}
		}
	}

	private static Set<String> template2Rule(Map<Integer, String> itemsetMappedToIndex) {
		Set<String> rules = new LinkedHashSet<String>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().split(" ").length >= 3) {
				rules.addAll(processRules(entry.getValue()));
			}
		}
		return rules;
	}

	private static void template2head(Map<Integer, String> itemsetMappedToIndex) {
		Set<String> rules = new LinkedHashSet<>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().split(" ").length >= 3) {
				String itemsetContents = entry.getValue();
				String[] itemsetSplit = itemsetContents.split(" ");
				double supportComb = itemset.get(itemsetContents);
				double support0 = itemset.get(itemsetSplit[0]);
				double support1 = itemset.get(itemsetSplit[1]);
				double support2 = itemset.get(itemsetSplit[2]);
				if (supportComb / support0 >= 0.6) {
					rules.add(itemsetSplit[0] + " --> {" + itemsetSplit[1] + ", " + itemsetSplit[2] + "}");
				}
				if (supportComb / support1 >= 0.6) {
					rules.add(itemsetSplit[1] + " --> {" + itemsetSplit[0] + ", " + itemsetSplit[2] + "}");
				}
				if (supportComb / support2 >= 0.6) {
					rules.add(itemsetSplit[2] + " --> {" + itemsetSplit[0] + ", " + itemsetSplit[1] + "}");
				}
			}
		}
		printRules(rules);
	}

	private static void template2body(Map<Integer, String> itemsetMappedToIndex) {
		Set<String> rules = new LinkedHashSet<>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().split(" ").length >= 3) {
				String itemsetContents = entry.getValue();
				String[] itemsetSplit = itemsetContents.split(" ");
				double supportComb = itemset.get(itemsetContents);
				double support3 = itemset.get(itemsetSplit[0] + " " + itemsetSplit[1]);
				double support4 = itemset.get(itemsetSplit[0] + " " + itemsetSplit[2]);
				double support5 = itemset.get(itemsetSplit[1] + " " + itemsetSplit[2]);
				if (supportComb / support3 >= 0.6) {
					rules.add("{" + itemsetSplit[0] + ", " + itemsetSplit[1] + "} --> " + itemsetSplit[2]);
				}
				if (supportComb / support4 >= 0.6) {
					rules.add("{" + itemsetSplit[0] + ", " + itemsetSplit[2] + "} --> " + itemsetSplit[1]);
				}
				if (supportComb / support5 >= 0.6) {
					rules.add("{" + itemsetSplit[1] + ", " + itemsetSplit[2] + "} --> " + itemsetSplit[0]);
				}
			}
		}
		printRules(rules);
	}

	private static Set<String> bodySingle(Map<Integer, String> itemsetMappedToIndex, String query) {
		Set<String> rules = new LinkedHashSet<>();
		Map<Integer, String> itemsetMappedToIndexTrim = new HashMap<>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().contains(query)) {
				itemsetMappedToIndexTrim.put(entry.getKey(), entry.getValue());
			}
		}
		for (String eachItemSet : itemsetMappedToIndexTrim.values()) {
			String[] itemsetSplit = eachItemSet.split(" ");
			if (itemsetSplit.length == 2) {
				double supportComb = itemset.get(eachItemSet);
				double support0 = itemset.get(query);
				if (supportComb / support0 >= 0.6) {
					if (itemsetSplit[0].equals(query)) {
						rules.add(query + " --> " + itemsetSplit[1]);
					} else {
						rules.add(query + " --> " + itemsetSplit[0]);
					}
				}
			} else if (itemsetSplit.length == 3) {
				double supportComb = itemset.get(eachItemSet);
				double support0 = itemset.get(query);
				String second = "";
				String third = "";
				if (itemsetSplit[0].equals(query)) {
					second = itemsetSplit[1];
					third = itemsetSplit[2];
				} else if (itemsetSplit[1].equals(query)) {
					second = itemsetSplit[0];
					third = itemsetSplit[2];
				} else if (itemsetSplit[2].equals(query)) {
					second = itemsetSplit[0];
					third = itemsetSplit[1];
				}
				double support3 = 0.0;
				double support4 = 0.0;
				if (itemset.get(query + " " + second) == null) {
					support3 = itemset.get(second + " " + query);
				} else {
					support3 = itemset.get(query + " " + second);
				}
				if (itemset.get(query + " " + third) == null) {
					support4 = itemset.get(third + " " + query);
				} else {
					support4 = itemset.get(query + " " + third);
				}
				if (supportComb / support0 >= 0.6) {
					rules.add(query + " --> {" + second + ", " + third + "}");
				}
				if (supportComb / support3 >= 0.6) {
					rules.add("{" + query + ", " + second + "} --> " + third);
				}
				if (supportComb / support4 >= 0.6) {
					rules.add("{" + query + ", " + third + "} --> " + second);
				}
			}
		}
		return rules;
	}

	private static Set<String> bodyNoneSingle(Map<Integer, String> itemsetMappedToIndex, String query) {
		Set<String> rules = new LinkedHashSet<>();
		for (String eachItemSet : itemsetMappedToIndex.values()) {
			if (!eachItemSet.contains(query)) {
				rules.addAll(processRules(eachItemSet));
			} else {
				String[] itemsetSplit = eachItemSet.split(" ");
				if (itemsetSplit.length == 2) {
					double supportComb = itemset.get(eachItemSet);
					String first = "";
					String second = "";
					if (itemsetSplit[0].equals(query)) {
						first = itemsetSplit[1];
						second = itemsetSplit[0];
					} else {
						first = itemsetSplit[0];
						second = itemsetSplit[1];
					}
					double support0 = itemset.get(first);
					if (supportComb / support0 >= 0.6) {
						rules.add(first + " --> " + second);
					}
				} else if (itemsetSplit.length == 3) {
					double supportComb = itemset.get(eachItemSet);
					String prohibited = "";
					String allowed1 = "";
					String allowed2 = "";
					if (itemsetSplit[0].equals(query)) {
						prohibited = itemsetSplit[0];
						allowed1 = itemsetSplit[1];
						allowed2 = itemsetSplit[2];
					} else if (itemsetSplit[1].equals(query)) {
						prohibited = itemsetSplit[1];
						allowed1 = itemsetSplit[0];
						allowed2 = itemsetSplit[2];
					} else if (itemsetSplit[2].equals(query)) {
						prohibited = itemsetSplit[2];
						allowed1 = itemsetSplit[0];
						allowed2 = itemsetSplit[1];
					}
					double support0 = itemset.get(allowed1);
					double support1 = itemset.get(allowed2);
					double support3 = 0.0;
					if (itemset.get(allowed1 + " " + allowed2) == null) {
						support3 = itemset.get(allowed2 + " " + allowed1);
					} else {
						support3 = itemset.get(allowed1 + " " + allowed2);
					}

					if (supportComb / support0 >= 0.6) {
						rules.add(allowed1 + " --> {" + prohibited + ", " + allowed2 + "}");
					}
					if (supportComb / support1 >= 0.6) {
						rules.add(allowed2 + " --> {" + prohibited + ", " + allowed1 + "}");
					}
					if (supportComb / support3 >= 0.6) {
						rules.add("{" + allowed1 + ", " + allowed2 + "} --> " + prohibited);
					}
				}
			}
		}
		return rules;
	}

	private static Set<String> bodyDouble(Map<Integer, String> itemsetMappedToIndex, String query) {
		Set<String> rules = new LinkedHashSet<>();
		String queryPart1 = query.split(",")[0];
		String queryPart2 = query.split(",")[1];
		Map<Integer, String> itemsetMappedToIndexTrim = new HashMap<>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().contains(queryPart1) && entry.getValue().contains(queryPart2)) {
				itemsetMappedToIndexTrim.put(entry.getKey(), entry.getValue());
			}
		}
		for (String eachItemSet : itemsetMappedToIndexTrim.values()) {
			String[] itemsetSplit = eachItemSet.split(" ");
			if (itemsetSplit.length == 3) {
				double supportComb = itemset.get(eachItemSet);
				String body1 = "";
				String body2 = "";
				String head = "";
				if (itemsetSplit[0].equals(queryPart1)) {
					body1 = itemsetSplit[0];
				} else if (itemsetSplit[0].equals(queryPart2)) {
					body2 = itemsetSplit[0];
				}
				if (itemsetSplit[1].equals(queryPart1)) {
					body1 = itemsetSplit[1];
				} else if (itemsetSplit[1].equals(queryPart2)) {
					body2 = itemsetSplit[1];
				}
				if (itemsetSplit[2].equals(queryPart1)) {
					body1 = itemsetSplit[2];
				} else if (itemsetSplit[2].equals(queryPart2)) {
					body2 = itemsetSplit[2];
				}
				head = eachItemSet.replaceAll(body1, "");
				head = head.replaceAll(body2, "");
				head = head.trim();
				double support3 = 0.0;
				if (itemset.get(body2 + " " + body1) == null) {
					support3 = itemset.get(body1 + " " + body2);
				} else {
					support3 = itemset.get(body2 + " " + body1);
				}
				if (supportComb / support3 >= 0.6) {
					rules.add("{" + body1 + ", " + body2 + "} --> " + head);
				}
			}
		}
		return rules;
	}

	private static Set<String> headSingle(Map<Integer, String> itemsetMappedToIndex, String query) {
		Set<String> rules = new LinkedHashSet<>();
		Map<Integer, String> itemsetMappedToIndexTrim = new HashMap<>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().contains(query)) {
				itemsetMappedToIndexTrim.put(entry.getKey(), entry.getValue());
			}
		}
		for (String eachItemSet : itemsetMappedToIndexTrim.values()) {
			String[] itemsetSplit = eachItemSet.split(" ");
			if (itemsetSplit.length == 2) {
				double supportComb = itemset.get(eachItemSet);
				String head = "";
				String body = "";
				if (itemsetSplit[0].equals(query)) {
					head = itemsetSplit[0];
					body = itemsetSplit[1];
				} else {
					head = itemsetSplit[1];
					body = itemsetSplit[0];
				}
				double support0 = itemset.get(body);
				if (supportComb / support0 >= 0.6) {
					rules.add(body + " --> " + head);
				}
			} else if (itemsetSplit.length == 3) {
				double supportComb = itemset.get(eachItemSet);
				String prohibited = "";
				String allowed1 = "";
				String allowed2 = "";
				if (itemsetSplit[0].equals(query)) {
					prohibited = itemsetSplit[0];
					allowed1 = itemsetSplit[1];
					allowed2 = itemsetSplit[2];
				} else if (itemsetSplit[1].equals(query)) {
					prohibited = itemsetSplit[1];
					allowed1 = itemsetSplit[0];
					allowed2 = itemsetSplit[2];
				} else if (itemsetSplit[2].equals(query)) {
					prohibited = itemsetSplit[2];
					allowed1 = itemsetSplit[0];
					allowed2 = itemsetSplit[1];
				}
				double support0 = itemset.get(allowed1);
				double support1 = itemset.get(allowed2);
				double support3 = 0.0;
				if (itemset.get(allowed1 + " " + allowed2) == null) {
					support3 = itemset.get(allowed2 + " " + allowed1);
				} else {
					support3 = itemset.get(allowed1 + " " + allowed2);
				}

				if (supportComb / support0 >= 0.6) {
					rules.add(allowed1 + " --> {" + prohibited + ", " + allowed2 + "}");
				}
				if (supportComb / support1 >= 0.6) {
					rules.add(allowed2 + " --> {" + prohibited + ", " + allowed1 + "}");
				}
				if (supportComb / support3 >= 0.6) {
					rules.add("{" + allowed1 + ", " + allowed2 + "} --> " + prohibited);
				}
			}
		}
		return rules;
	}

	private static void headNotDouble(Map<Integer, String> itemsetMappedToIndex, String query) {
		Set<String> rules = new LinkedHashSet<>();
		String queryPart1 = query.split(",")[0];
		String queryPart2 = query.split(",")[1];

		for (String eachItemSet : itemsetMappedToIndex.values()) {
			if (!eachItemSet.contains(queryPart1) && !eachItemSet.contains(queryPart2)) {
				rules.addAll(processRules(eachItemSet));
			} else {
				String[] itemsetSplit = eachItemSet.split(" ");
				if (itemsetSplit.length == 2 && !eachItemSet.equals(queryPart1 + " " + queryPart2)
						&& !eachItemSet.equals(queryPart2 + " " + queryPart1)) {
					double supportComb = itemset.get(eachItemSet);
					String first = "";
					String second = "";
					if (itemsetSplit[0].equals(queryPart1) || itemsetSplit[0].equals(queryPart2)) {
						first = itemsetSplit[0];
						second = itemsetSplit[1];
					} else {
						first = itemsetSplit[1];
						second = itemsetSplit[0];
					}
					double support0 = itemset.get(first);
					if (supportComb / support0 >= 0.6) {
						rules.add(first + " --> " + second);
					}
				} else if (itemsetSplit.length == 3) {
					rules.addAll(processRules(eachItemSet));
				}
			}
		}

		printRules(rules);
	}

	private static void headDouble(Map<Integer, String> itemsetMappedToIndex, String query) {
		Set<String> rules = new LinkedHashSet<>();
		String queryPart1 = query.split(",")[0];
		String queryPart2 = query.split(",")[1];

		Map<Integer, String> itemsetMappedToIndexTrim = new HashMap<>();
		for (Map.Entry<Integer, String> entry : itemsetMappedToIndex.entrySet()) {
			if (entry.getValue().contains(queryPart1) && entry.getValue().contains(queryPart2)) {
				itemsetMappedToIndexTrim.put(entry.getKey(), entry.getValue());
			}
		}
		for (String eachItemSet : itemsetMappedToIndexTrim.values()) {
			String[] itemsetSplit = eachItemSet.split(" ");
			if (itemsetSplit.length == 2) {
				double supportComb = itemset.get(eachItemSet);
				String head = "";
				String body = "";
				if (itemsetSplit[0].equals(query)) {
					head = itemsetSplit[0];
					body = itemsetSplit[1];
				} else {
					head = itemsetSplit[1];
					body = itemsetSplit[0];
				}
				double support0 = itemset.get(body);
				if (supportComb / support0 >= 0.6) {
					rules.add(body + " --> " + head);
				}
			} else if (itemsetSplit.length == 3) {
				rules.addAll(processRules(eachItemSet));
			}
		}
		printRules(rules);
	}
}
