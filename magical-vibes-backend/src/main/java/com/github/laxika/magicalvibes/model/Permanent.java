package com.github.laxika.magicalvibes.model;

import lombok.Getter;

@Getter
public class Permanent {

    private final Card card;
    private boolean tapped;

    public Permanent(Card card) {
        this.card = card;
        this.tapped = false;
    }

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        this.tapped = false;
    }
}
