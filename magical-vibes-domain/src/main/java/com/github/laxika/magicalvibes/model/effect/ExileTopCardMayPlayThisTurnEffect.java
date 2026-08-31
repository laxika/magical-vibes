package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top {@code count} cards of the controller's library. Until end of turn, the controller
 * may play those cards (any type, lands included). When {@code withoutPayingManaCost} is
 * {@code true} the play is free; otherwise it is played at its normal costs and timing. When
 * {@code freeCastFilter} is non-null, only matching cards are free; all other exiled cards retain
 * normal play permission (Nahiri, Forged in Fury). The count is evaluated when the effect resolves.
 * <p>
 * Grants {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireEndOfTurn} (and, for the
 * free variant, {@code exilePlayWithoutPayingManaCost}). The filter is used for mixed normal/free
 * permission effects; use {@link ExileTopCardsMayCastMatchingThisTurnEffect} when non-matching
 * cards should not receive play permission at all.
 */
public record ExileTopCardMayPlayThisTurnEffect(
        DynamicAmount count,
        boolean withoutPayingManaCost,
        CardPredicate freeCastFilter
) implements CardEffect {

    /** Single-card variant (Oracle's Vault). */
    public ExileTopCardMayPlayThisTurnEffect(boolean withoutPayingManaCost) {
        this(new Fixed(1), withoutPayingManaCost, null);
    }

    public ExileTopCardMayPlayThisTurnEffect(int count, boolean withoutPayingManaCost) {
        this(new Fixed(count), withoutPayingManaCost, null);
    }

    public ExileTopCardMayPlayThisTurnEffect(DynamicAmount count, boolean withoutPayingManaCost) {
        this(count, withoutPayingManaCost, null);
    }

    public ExileTopCardMayPlayThisTurnEffect(
            int count,
            boolean withoutPayingManaCost,
            CardPredicate freeCastFilter
    ) {
        this(new Fixed(count), withoutPayingManaCost, freeCastFilter);
    }
}
