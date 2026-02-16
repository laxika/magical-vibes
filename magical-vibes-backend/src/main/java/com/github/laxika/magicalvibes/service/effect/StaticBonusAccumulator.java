package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.HashSet;
import java.util.Set;

public class StaticBonusAccumulator {

    private int power;
    private int toughness;
    private final Set<Keyword> keywords = new HashSet<>();
    private boolean animatedCreature;

    public void addPower(int amount) {
        power += amount;
    }

    public void addToughness(int amount) {
        toughness += amount;
    }

    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
    }

    public void addKeywords(Set<Keyword> keywords) {
        this.keywords.addAll(keywords);
    }

    public void setAnimatedCreature(boolean animatedCreature) {
        this.animatedCreature = animatedCreature;
    }

    public int getPower() {
        return power;
    }

    public int getToughness() {
        return toughness;
    }

    public Set<Keyword> getKeywords() {
        return keywords;
    }

    public boolean isAnimatedCreature() {
        return animatedCreature;
    }
}
