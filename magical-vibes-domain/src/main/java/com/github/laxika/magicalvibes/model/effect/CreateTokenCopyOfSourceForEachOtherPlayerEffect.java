package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Creates a token copy of the source permanent for each player other than its controller.
 *
 * @param copyProfile how the copies are made
 * @param creators    the players still to create their copy, in turn order, or {@code null} for every player other
 *                    than the source's controller: set only by the engine, on the step it inserts to go on with the
 *                    players after one whose "first time you would create tokens" replacement stopped the resolution
 *                    for a choice of its own (Esix, Fractal Bloom's creature)
 */
public record CreateTokenCopyOfSourceForEachOtherPlayerEffect(
        CreateTokenCopyOfTargetPermanentEffect copyProfile, List<UUID> creators) implements CardEffect {

    public CreateTokenCopyOfSourceForEachOtherPlayerEffect {
        creators = creators == null ? null : List.copyOf(creators);
    }

    public CreateTokenCopyOfSourceForEachOtherPlayerEffect(CreateTokenCopyOfTargetPermanentEffect copyProfile) {
        this(copyProfile, null);
    }

    public CreateTokenCopyOfSourceForEachOtherPlayerEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect());
    }

    /** The same effect for {@code creators} only, the players after the one whose replacement stopped it. */
    public CreateTokenCopyOfSourceForEachOtherPlayerEffect forCreators(List<UUID> creators) {
        return new CreateTokenCopyOfSourceForEachOtherPlayerEffect(copyProfile, creators);
    }
}
