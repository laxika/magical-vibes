package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top {@code count} cards of the controller's library. Until end of turn, the controller
 * may play those cards (any type, lands included). When {@code withoutPayingManaCost} is
 * {@code true} the play is free — the controller casts it without paying its mana cost (Oracle's
 * Vault's second ability); otherwise it is played at its normal costs and timing (Oracle's Vault's
 * first ability, Act on Impulse with {@code count} 3).
 * <p>
 * Grants {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireEndOfTurn} (and, for the
 * free variant, {@code exilePlayWithoutPayingManaCost}). Unlike
 * {@link ExileTopCardMayCastNonlandThisTurnEffect} (Vance's Blasting Cannons) the permission is
 * granted for lands as well as nonland cards.
 */
public record ExileTopCardMayPlayThisTurnEffect(int count, boolean withoutPayingManaCost)
        implements CardEffect {

    /** Single-card variant (Oracle's Vault). */
    public ExileTopCardMayPlayThisTurnEffect(boolean withoutPayingManaCost) {
        this(1, withoutPayingManaCost);
    }
}
