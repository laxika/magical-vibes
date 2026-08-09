package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller looks at the top {@code count} cards of the selected library, puts any number on
 * the bottom in any order, and puts the rest on top in any order. Amount may be fixed ("scry 2") or
 * dynamic ("scry X, where X is the number of Zombies you control").
 */
public record ScryEffect(DynamicAmount count, LibraryOwner owner) implements CardEffect {

    public ScryEffect(DynamicAmount count) {
        this(count, LibraryOwner.CONTROLLER);
    }

    public ScryEffect(int count) {
        this(new Fixed(count), LibraryOwner.CONTROLLER);
    }

    public ScryEffect(int count, LibraryOwner owner) {
        this(new Fixed(count), owner);
    }

    @Override
    public TargetSpec targetSpec() {
        return owner == LibraryOwner.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
