package com.github.laxika.magicalvibes.model.effect;

/** Copies cards exiled with the source permanent and offers the copies to the controller. */
public record CopyCardsExiledWithSourceAndMayCastCopiesEffect(boolean copyAll, CopyCastCost castCost)
        implements CardEffect {

    public enum CopyCastCost {
        NORMAL,
        ONE_GENERIC,
        FREE
    }

    public CopyCardsExiledWithSourceAndMayCastCopiesEffect {
        if (castCost == null) {
            throw new IllegalArgumentException("castCost must not be null");
        }
    }

    public static CopyCardsExiledWithSourceAndMayCastCopiesEffect oneForNormalCost() {
        return new CopyCardsExiledWithSourceAndMayCastCopiesEffect(false, CopyCastCost.NORMAL);
    }

    public static CopyCardsExiledWithSourceAndMayCastCopiesEffect oneForOneGeneric() {
        return new CopyCardsExiledWithSourceAndMayCastCopiesEffect(false, CopyCastCost.ONE_GENERIC);
    }

    public static CopyCardsExiledWithSourceAndMayCastCopiesEffect allForFree() {
        return new CopyCardsExiledWithSourceAndMayCastCopiesEffect(true, CopyCastCost.FREE);
    }
}
