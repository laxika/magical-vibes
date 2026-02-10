package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Permanent {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final int id;
    private final Card card;
    private boolean tapped;
    private boolean attacking;
    private boolean blocking;
    private int blockingTarget = -1;
    private boolean summoningSick;
    @Setter private int powerModifier;
    @Setter private int toughnessModifier;

    public Permanent(Card card) {
        this.id = ID_COUNTER.incrementAndGet();
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

    public void resetModifiers() {
        this.powerModifier = 0;
        this.toughnessModifier = 0;
    }
}
