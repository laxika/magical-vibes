package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Creates the listed token profiles, then offers one optional Aura from hand or graveyard for each token. */
public record CreateTokensAndAttachAurasEffect(List<CreateTokenEffect> tokenEffects) implements CardEffect {

    public CreateTokensAndAttachAurasEffect {
        tokenEffects = List.copyOf(tokenEffects);
    }
}
