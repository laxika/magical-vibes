package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The target player reveals their hand, the controller chooses a card in it matching
 * {@code choosableFilter}, then that player's graveyard, hand, and library are searched for
 * cards with the same name as the chosen card. By default <b>all</b> such cards are exiled
 * (mandatory); when {@code chooseAnyNumber} is true, the chosen card is exiled and the controller
 * chooses any number of the remaining matching cards to exile. Then that player shuffles.
 * <p>
 * Differs from {@link ChooseNameRevealHandDamagePerCopyAndExileEffect} (Thought Hemorrhage): there
 * the name is chosen blind from every name in the game and the source deals damage per revealed
 * copy; here the choice is restricted to cards actually in the revealed hand and no damage is
 * dealt. Nothing happens when the revealed hand holds no legal card.
 * <p>
 * Used by: Shimian Specter (nonland cards), Lobotomy (cards other than basic lands), and Pick the
 * Brain (nonland cards, any-number delirium branch).
 */
public record RevealHandChooseCardFromItAndExileAllCopiesEffect(CardPredicate choosableFilter,
                                                                 boolean chooseAnyNumber)
        implements CombatDamageTriggerContextEffect {

    public RevealHandChooseCardFromItAndExileAllCopiesEffect(CardPredicate choosableFilter) {
        this(choosableFilter, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
