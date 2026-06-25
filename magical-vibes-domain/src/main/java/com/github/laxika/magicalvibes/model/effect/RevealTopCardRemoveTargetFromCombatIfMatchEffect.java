package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the controller's library. If the revealed card matches the given
 * {@link CardPredicate}, the permanent referenced by the stack entry's {@code targetId} is removed
 * from combat. The revealed card is then put on the bottom of the controller's library regardless.
 *
 * <p>The "target" here is the attacking creature that triggered the ability — it is set on the
 * stack entry by the engine, not chosen by a player, so this is a non-targeting effect.
 *
 * <p>Used by Lost in the Woods ("Whenever a creature attacks you or a planeswalker you control,
 * reveal the top card of your library. If it's a Forest card, remove that creature from combat.
 * Then put the revealed card on the bottom of your library.").
 */
public record RevealTopCardRemoveTargetFromCombatIfMatchEffect(CardPredicate matchPredicate) implements CardEffect {
}
