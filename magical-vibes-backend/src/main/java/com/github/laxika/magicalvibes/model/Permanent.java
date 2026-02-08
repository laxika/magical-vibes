package com.github.laxika.magicalvibes.model;

import lombok.Getter;

@Getter
public class Permanent {

    private final Card card;
    private boolean tapped;
    private boolean attacking;
    private boolean blocking;
    private int blockingTarget = -1;
    private boolean summoningSick;

    public Permanent(Card card) {
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
}
