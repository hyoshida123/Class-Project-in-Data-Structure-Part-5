package spellchecker;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;

public class BasicDictionary implements Dictionary {

	private BinaryTreeNode root;
	private BinaryTreeNode cursor;
	private StringBuilder sb;
	private int count;

	@Override
	public void importFile(String filename) throws Exception {
		String str = FileUtils.readFileToString(new File(filename));
		String[] strToken = str.split("\n");
		ArrayList<String> data = new ArrayList<String>();
		for (int i = 0; i < strToken.length; i++) {
			data.add(strToken[i].trim());
			count++;
		}
		// add(data.get(round(count / 2) - 1));
		formCmpTree(data, data.size() / 2, 0, data.size() - 1);
		// Collections.shuffle(data);
		// for (String e : data) {
		// add(e);
		// }
	}

	private void formCmpTree(ArrayList<String> list, int root, int start, int end) {
		add(list.get(root));
		if (start < end)
			formCmpTree(list, (start + end) / 2, start, root - 1);
		if (start <= end)
			formCmpTree(list, (start + end) / 2, root + 1, end);
	}

	@Override
	public void load(String filename) throws Exception {
		String string = FileUtils.readFileToString(new File(filename));
		String[] data = string.split("\n");
		for (String e : data) {
			add(e.trim());
		}
	}

	@Override
	public void save(String filename) throws Exception {
		sb = new StringBuilder();
		saveTreePreOrder(root);
		FileUtils.writeStringToFile(new File(filename), sb.toString());
	}

	private void saveTreePreOrder(BinaryTreeNode cur) {
		if (cur == null)
			return;
		sb.append(cur.value + "\n");
		saveTreePreOrder(cur.left);
		saveTreePreOrder(cur.right);
	}

	@Override
	public String[] find(String word) {
		cursor = root;
		String[] found = { "", "" };
		if (cursor == null)
			return null;
		while (cursor != null) {
			if (word.compareToIgnoreCase(cursor.value) > 0) {
				found[0] = cursor.value;
				cursor = cursor.right;
			} else if (word.compareToIgnoreCase(cursor.value) < 0) {
				found[1] = cursor.value;
				cursor = cursor.left;
			} else
				return null;
		}
		return found;
	}

	@Override
	public void add(String word) {
		BinaryTreeNode newNode = new BinaryTreeNode(word);
		newNode.left = null;
		newNode.right = null;
		cursor = root;
		if (root == null)
			root = newNode;
		else {
			while (cursor != null) {
				if (word.compareToIgnoreCase(cursor.value) > 0) {
					// Check whether a node is null before insert.
					if (cursor.right == null) {
						// If it's null, insert and return.
						cursor.right = newNode;
						return;
					}
					cursor = cursor.right;
				} else if (word.compareToIgnoreCase(cursor.value) == 0) {
					// If the tree has already contained the word, return.
					return;
				} else {
					// Check whether a node is null before insert.
					if (cursor.left == null) {
						// If it's null, insert and return.
						cursor.left = newNode;
						return;
					}
					cursor = cursor.left;
				}
			}
		}
	}

	@Override
	public BinaryTreeNode getRoot() {
		return root;
	}

	@Override
	public int getCount() {
		return count;
	}

}
