package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Beginning-of-combat choice where each opponent may tap an untapped creature they control. The
 * first acceptance taps the source and creates one token; later opponents still receive their
 * choices, but cannot create another token from the same resolution.
 */
public record AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect(
        CreateTokenEffect tokenTemplate,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        boolean anyAccepted
) implements TokenCreatingEffect {

    public AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect(CreateTokenEffect tokenTemplate) {
        this(tokenTemplate, null, null, null, false);
    }

    public AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect {
        Objects.requireNonNull(tokenTemplate, "tokenTemplate is required");
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }
}
