package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Special-action effect that ends a source permanent's active ransom control effect. */
public record PayRansomEffect(UUID ransomSourcePermanentId) implements CardEffect {

    @Override
    public boolean isSpecialAction() {
        return true;
    }
}
