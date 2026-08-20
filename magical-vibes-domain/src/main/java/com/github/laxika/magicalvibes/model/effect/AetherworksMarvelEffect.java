package com.github.laxika.magicalvibes.model.effect;

/** Looks at the top cards and offers a spell among them for casting without paying its mana cost. */
public record AetherworksMarvelEffect(int lookCount) implements CardEffect {

    /** The original Aetherworks Marvel wording looks at six cards. */
    public AetherworksMarvelEffect() {
        this(6);
    }
}
