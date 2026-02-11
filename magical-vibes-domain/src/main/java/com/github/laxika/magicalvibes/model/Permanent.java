package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Permanent {

    private final UUID id;
    private final Card card;
    private boolean tapped;
    private boolean attacking;
    private boolean blocking;
    private int blockingTarget = -1;
    private boolean summoningSick;
    @Setter private int powerModifier;
    @Setter private int toughnessModifier;
    @Setter private int damagePreventionShield;
    private final Set<Keyword> grantedKeywords = new HashSet<>();

    public Permanent(Card card) {
        this.id = UUID.randomUUID();
        this.card = card;
        this.tapped = false;
        this.summoningSick = true;
    }

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        this.tapped = false;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void setBlockingTarget(int blockingTarget) {
        this.blockingTarget = blockingTarget;
    }

    public void setSummoningSick(boolean summoningSick) {
        this.summoningSick = summoningSick;
    }

    public void clearCombatState() {
        this.attacking = false;
        this.blocking = false;
        this.blockingTarget = -1;
    }

    public int getEffectivePower() {
        return (card.getPower() != null ? card.getPower() : 0) + powerModifier;
    }

    public int getEffectiveToughness() {
        return (card.getToughness() != null ? card.getToughness() : 0) + toughnessModifier;
    }

    public boolean hasKeyword(Keyword keyword) {
        return card.getKeywords().contains(keyword) || grantedKeywords.contains(keyword);
    }

    public void resetModifiers() {
        this.powerModifier = 0;
        this.toughnessModifier = 0;
        this.grantedKeywords.clear();
    }
}
