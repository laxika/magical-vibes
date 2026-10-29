package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Creates token copies of the permanent attached to an Aura using the supplied copy profile.
 *
 * @param useTriggeringCreatureLastKnown when true and both the Aura and the creature have left before
 *                                       resolution, copy the creature the trigger was about as it last
 *                                       existed ("create a token that's a copy of that creature", Mirror
 *                                       Mockery). When false, nothing is created once the Aura is unattached
 *                                       (Followed Footsteps, Mechanized Production).
 */
public record CreateTokenCopyOfEnchantedPermanentEffect(
        int amount, UUID auraPermanentId, CreateTokenCopyOfTargetPermanentEffect copyEffect,
        boolean useTriggeringCreatureLastKnown
) implements CardEffect {

    public CreateTokenCopyOfEnchantedPermanentEffect(int amount, UUID auraPermanentId,
                                                     CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this(amount, auraPermanentId, copyEffect, false);
    }

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

    /** Like {@link #exiledAtEndOfCombat()}, but copies "that creature" by last-known information if it left. */
    public static CreateTokenCopyOfEnchantedPermanentEffect ofTriggeringCreatureExiledAtEndOfCombat() {
        return new CreateTokenCopyOfEnchantedPermanentEffect(1, null,
                CreateTokenCopyOfTargetPermanentEffect.exiledAtEndOfCombat(), true);
    }
}
