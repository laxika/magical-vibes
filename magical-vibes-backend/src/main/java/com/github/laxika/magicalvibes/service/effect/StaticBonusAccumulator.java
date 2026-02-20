package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaticBonusAccumulator {

    private int power;
    private int toughness;
    private final Set<Keyword> keywords = new HashSet<>();
    private boolean animatedCreature;
    private final List<ActivatedAbility> grantedActivatedAbilities = new ArrayList<>();

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

    public void addActivatedAbility(ActivatedAbility ability) {
        grantedActivatedAbilities.add(ability);
    }

    public List<ActivatedAbility> getGrantedActivatedAbilities() {
        return grantedActivatedAbilities;
    }
}

