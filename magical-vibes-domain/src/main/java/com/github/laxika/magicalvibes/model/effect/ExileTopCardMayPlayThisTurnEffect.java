package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top card of the controller's library. Until end of turn, the controller may play that
 * card (any type, lands included). When {@code withoutPayingManaCost} is {@code true} the play is
 * free — the controller casts it without paying its mana cost (Oracle's Vault's second ability);
 * otherwise it is played at its normal costs and timing (Oracle's Vault's first ability).
 * <p>
 * Grants {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireEndOfTurn} (and, for the
 * free variant, {@code exilePlayWithoutPayingManaCost}). Unlike
 * {@link ExileTopCardMayCastNonlandThisTurnEffect} (Vance's Blasting Cannons) the permission is
 * granted for lands as well as nonland cards.
 */
public record ExileTopCardMayPlayThisTurnEffect(boolean withoutPayingManaCost) implements CardEffect {
}
