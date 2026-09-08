package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.laxika.magicalvibes.model.CardSubtype;

/** Creates token copies of the permanent attached to an Aura using the supplied copy profile. */
public record CreateTokenCopyOfEnchantedPermanentEffect(
        int amount, UUID auraPermanentId, CreateTokenCopyOfTargetPermanentEffect copyEffect
) implements CardEffect {

    public CreateTokenCopyOfEnchantedPermanentEffect() {
        this(1, null, new CreateTokenCopyOfTargetPermanentEffect());
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(UUID auraPermanentId) {
        this(1, auraPermanentId, new CreateTokenCopyOfTargetPermanentEffect());
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(int amount) {
        this(amount, null, new CreateTokenCopyOfTargetPermanentEffect());
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(int amount, UUID auraPermanentId) {
        this(amount, auraPermanentId, new CreateTokenCopyOfTargetPermanentEffect());
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this(1, null, copyEffect);
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(List<CardSubtype> additionalSubtypes) {
        this(new CreateTokenCopyOfTargetPermanentEffect(additionalSubtypes, Set.of(), null, null, Map.of()));
    }

    public static CreateTokenCopyOfEnchantedPermanentEffect exiledAtEndOfCombat() {
        return new CreateTokenCopyOfEnchantedPermanentEffect(
                CreateTokenCopyOfTargetPermanentEffect.exiledAtEndOfCombat());
    }
}
