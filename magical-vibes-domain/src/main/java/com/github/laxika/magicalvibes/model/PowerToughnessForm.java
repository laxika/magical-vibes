package com.github.laxika.magicalvibes.model;

import java.util.Set;

/** A named base power/toughness option for a permanent's entry or face-up choice. */
public record PowerToughnessForm(String label, int power, int toughness, Set<Keyword> keywords) {

    public PowerToughnessForm(String label, int power, int toughness) {
        this(label, power, toughness, Set.of());
    }

    public PowerToughnessForm {
        keywords = Set.copyOf(keywords);
    }
}
