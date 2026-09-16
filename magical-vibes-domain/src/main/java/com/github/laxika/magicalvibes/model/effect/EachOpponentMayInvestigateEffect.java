package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.UUID;

/** Each opponent may investigate; opponents who decline lose 1 life, then the controller investigates for the counted amount. */
public record EachOpponentMayInvestigateEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        int investigatedOpponentCount
) implements TokenCreatingEffect {

    public EachOpponentMayInvestigateEffect() {
        this(null, null, 0);
    }

    public EachOpponentMayInvestigateEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return CardType.ARTIFACT;
    }

    @Override
    public int tokenPower() {
        return 0;
    }

    @Override
    public int tokenToughness() {
        return 0;
    }
}
