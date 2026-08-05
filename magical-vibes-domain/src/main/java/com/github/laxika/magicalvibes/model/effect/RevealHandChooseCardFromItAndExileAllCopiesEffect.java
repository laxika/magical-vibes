package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * The target player reveals their hand, the controller chooses a card in it that isn't of any of
 * {@code excludedTypes}, then that player's graveyard, hand, and library are searched for
 * <b>all</b> cards with the same name as the chosen card and those are exiled (no choice —
 * mandatory). Then that player shuffles.
 * <p>
 * Differs from {@link ChooseNameRevealHandDamagePerCopyAndExileEffect} (Thought Hemorrhage): there
 * the name is chosen blind from every name in the game and the source deals damage per revealed
 * copy; here the choice is restricted to cards actually in the revealed hand and no damage is
 * dealt. Nothing happens when the revealed hand holds no legal card.
 * <p>
 * Used by: Shimian Specter (excluding lands).
 */
public record RevealHandChooseCardFromItAndExileAllCopiesEffect(List<CardType> excludedTypes)
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
