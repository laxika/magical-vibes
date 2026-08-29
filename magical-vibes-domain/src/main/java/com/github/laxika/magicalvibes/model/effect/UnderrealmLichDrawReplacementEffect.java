package com.github.laxika.magicalvibes.model.effect;

/** Underrealm Lich's mandatory replacement of each draw with a top-three hand/graveyard choice. */
public record UnderrealmLichDrawReplacementEffect()
        implements LookAtTopCardsChooseOneToHandDrawReplacementEffect {

    @Override
    public int lookCount() {
        return 3;
    }

    @Override
    public boolean restToGraveyard() {
        return true;
    }
}
