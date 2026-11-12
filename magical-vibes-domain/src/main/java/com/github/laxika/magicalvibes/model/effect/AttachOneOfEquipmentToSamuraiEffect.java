package com.github.laxika.magicalvibes.model.effect;


/** Attaches one of the listed Equipment permanents to a Samurai controlled by the ability controller. */
public record AttachOneOfEquipmentToSamuraiEffect()
        implements CardEffect, LibrarySelectionFollowUp {

    @Override
    public CardEffect effect() {
        return this;
    }

    @Override
    public String prompt() {
        return "Attach one of those Equipment to a Samurai you control?";
    }
}
