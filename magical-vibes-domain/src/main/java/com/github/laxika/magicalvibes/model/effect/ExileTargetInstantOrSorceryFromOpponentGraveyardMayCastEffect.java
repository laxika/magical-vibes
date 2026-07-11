package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a targeted instant or sorcery card from an opponent's graveyard, then grants its
 * controller permission to cast it this turn, spending mana of any type; if that spell would be
 * put into a graveyard, it is exiled instead.
 * <p>
 * Used by Nita, Forum Conciliator: "Exile target instant or sorcery card from an opponent's
 * graveyard. You may cast it this turn, and mana of any type can be spent to cast that spell. If
 * that spell would be put into a graveyard, exile it instead."
 */
public record ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect() implements CardEffect {

    @Override
    public boolean canTargetGraveyard() {
        return true;
    }

    @Override
    public boolean canTargetAnyGraveyard() {
        return true;
    }
}
