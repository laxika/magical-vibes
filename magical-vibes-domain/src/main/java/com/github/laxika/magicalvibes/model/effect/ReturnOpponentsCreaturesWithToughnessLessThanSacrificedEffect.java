package com.github.laxika.magicalvibes.model.effect;

/** Returns opposing creatures whose effective toughness is less than the toughness captured from
 * the creature sacrificed for the resolving exploit ability. */
public record ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffect()
        implements RemovalEffect, BoardWipeEffect {

    @Override
    public RemovalKind removalKind() {
        return null;
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
