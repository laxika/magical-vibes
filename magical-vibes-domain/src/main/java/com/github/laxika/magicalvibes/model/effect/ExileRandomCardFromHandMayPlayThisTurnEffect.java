package com.github.laxika.magicalvibes.model.effect;

/**
 * That player exiles a card at random from their hand. They may play that card this turn. At the
 * beginning of the next end step, if they haven't played it, they put it into their graveyard
 * (Elkin Lair).
 * <p>
 * The affected player is the stack entry's {@code targetId} (set to the active player by
 * {@code EACH_UPKEEP_TRIGGERED}). Empty hand is a no-op. Play permission uses
 * {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireEndOfTurn}; unplayed cleanup is
 * an {@code ExileToOwnerGraveyardAtNextEndStep} delayed action.
 */
public record ExileRandomCardFromHandMayPlayThisTurnEffect() implements CardEffect {
}
