package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaticBonusAccumulator {

    private int power;
    private int toughness;
    private final Set<Keyword> keywords = new HashSet<>();
    private final Set<CardColor> protectionColors = EnumSet.noneOf(CardColor.class);
    private boolean animatedCreature;
    private final List<ActivatedAbility> grantedActivatedAbilities = new ArrayList<>();
    private final List<CardEffect> grantedEffects = new ArrayList<>();
    private final Set<CardColor> grantedColors = EnumSet.noneOf(CardColor.class);
    private final List<CardSubtype> grantedSubtypes = new ArrayList<>();
    private boolean colorOverriding;
    private boolean subtypeOverriding;

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

    public void addProtectionColors(Set<CardColor> colors) {
        protectionColors.addAll(colors);
    }

    public Set<CardColor> getProtectionColors() {
        return protectionColors;
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        grantedActivatedAbilities.add(ability);
    }

    public List<ActivatedAbility> getGrantedActivatedAbilities() {
        return grantedActivatedAbilities;
    }

    public void addGrantedEffect(CardEffect effect) {
        grantedEffects.add(effect);
    }

    public List<CardEffect> getGrantedEffects() {
        return grantedEffects;
    }

    public void addGrantedColor(CardColor color) {
        grantedColors.add(color);
    }

    public Set<CardColor> getGrantedColors() {
        return grantedColors;
    }

    public void addGrantedSubtype(CardSubtype subtype) {
        if (!grantedSubtypes.contains(subtype)) {
            grantedSubtypes.add(subtype);
        }
    }

    public List<CardSubtype> getGrantedSubtypes() {
        return grantedSubtypes;
    }

    public boolean isColorOverriding() {
        return colorOverriding;
    }

    public void setColorOverriding(boolean colorOverriding) {
        this.colorOverriding = colorOverriding;
    }

    public boolean isSubtypeOverriding() {
        return subtypeOverriding;
    }

    public void setSubtypeOverriding(boolean subtypeOverriding) {
        this.subtypeOverriding = subtypeOverriding;
    }
}

