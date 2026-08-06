package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The target player reveals their hand, the controller chooses a card in it matching
 * {@code choosableFilter}, then that player's graveyard, hand, and library are searched for
 * <b>all</b> cards with the same name as the chosen card and those are exiled (no choice —
 * mandatory). Then that player shuffles.
 * <p>
 * Differs from {@link ChooseNameRevealHandDamagePerCopyAndExileEffect} (Thought Hemorrhage): there
 * the name is chosen blind from every name in the game and the source deals damage per revealed
 * copy; here the choice is restricted to cards actually in the revealed hand and no damage is
 * dealt. Nothing happens when the revealed hand holds no legal card.
 * <p>
 * Used by: Shimian Specter (nonland cards), Lobotomy (cards other than basic lands).
 */
public record RevealHandChooseCardFromItAndExileAllCopiesEffect(CardPredicate choosableFilter)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
