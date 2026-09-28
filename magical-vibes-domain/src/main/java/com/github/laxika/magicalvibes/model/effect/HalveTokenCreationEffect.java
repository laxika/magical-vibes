package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import java.util.UUID;

/** Halving Season's replacement effect for tokens created by an opponent. */
public record HalveTokenCreationEffect() implements TokenCreationReplacementEffect {

    @Override
    public CardSubtype affectedSubtype() {
        return null;
    }

    @Override
    public int replaceTokenCount(int currentCount) {
        return currentCount > 0 ? currentCount / 2 : currentCount;
    }

    @Override
    public boolean appliesToTokenCreator(UUID sourceControllerId, UUID tokenCreatorId) {
        return sourceControllerId != null && !sourceControllerId.equals(tokenCreatorId);
    }
}
