package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;

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
    private boolean selfBecomeCreature;
    private final List<ActivatedAbility> grantedActivatedAbilities = new ArrayList<>();
    private final List<CardEffect> grantedEffects = new ArrayList<>();
    private final Set<CardColor> grantedColors = EnumSet.noneOf(CardColor.class);
    private final List<CardSubtype> grantedSubtypes = new ArrayList<>();
    private final Set<CardType> grantedCardTypes = EnumSet.noneOf(CardType.class);
    private final Set<Keyword> removedKeywords = new HashSet<>();
    private boolean colorOverriding;
    private boolean subtypeOverriding;
    private boolean landSubtypeOverriding;
    private boolean basePTOverridden;
    private int basePowerOverride;
    private int baseToughnessOverride;
    private boolean losesAllAbilities;

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

    public void setSelfBecomeCreature(boolean selfBecomeCreature) {
        this.selfBecomeCreature = selfBecomeCreature;
    }

    public boolean isSelfBecomeCreature() {
        return selfBecomeCreature;
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

    public void addGrantedCardType(CardType cardType) {
        grantedCardTypes.add(cardType);
    }

    public Set<CardType> getGrantedCardTypes() {
        return grantedCardTypes;
    }

    public void removeKeyword(Keyword keyword) {
        removedKeywords.add(keyword);
    }

    public Set<Keyword> getRemovedKeywords() {
        return removedKeywords;
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

    public boolean isLandSubtypeOverriding() {
        return landSubtypeOverriding;
    }

    public void setLandSubtypeOverriding(boolean landSubtypeOverriding) {
        this.landSubtypeOverriding = landSubtypeOverriding;
    }

    public boolean isBasePTOverridden() {
        return basePTOverridden;
    }

    public int getBasePowerOverride() {
        return basePowerOverride;
    }

    public int getBaseToughnessOverride() {
        return baseToughnessOverride;
    }

    public void setBasePTOverride(int power, int toughness) {
        this.basePTOverridden = true;
        this.basePowerOverride = power;
        this.baseToughnessOverride = toughness;
    }

    public boolean isLosesAllAbilities() {
        return losesAllAbilities;
    }

    public void setLosesAllAbilities(boolean losesAllAbilities) {
        this.losesAllAbilities = losesAllAbilities;
    }

    /**
     * Builds a {@link StaticBonus} from this accumulator's state.
     *
     * @param finalPower     the computed power (accumulator power + any external adjustments)
     * @param finalToughness the computed toughness (accumulator toughness + any external adjustments)
     * @param animated       whether the target should be treated as an animated creature
     */
    public StaticBonus toStaticBonus(int finalPower, int finalToughness, boolean animated) {
        return new StaticBonus(
                finalPower, finalToughness, keywords, protectionColors,
                animated, grantedActivatedAbilities, grantedEffects,
                grantedColors, grantedSubtypes, grantedCardTypes, colorOverriding, subtypeOverriding, landSubtypeOverriding, removedKeywords,
                basePTOverridden, basePowerOverride, baseToughnessOverride, losesAllAbilities);
    }
}

