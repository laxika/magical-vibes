package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/** Copies the spell that caused this spell-copy trigger for its chosen player target. */
public record CopyCopiedSpellForTargetPlayerEffect(StackEntry spellSnapshot)
        implements CopiedSpellReferencingEffect {

    public CopyCopiedSpellForTargetPlayerEffect() {
        this(null);
    }

    @Override
    public CardEffect snapshotCopiedSpell(StackEntry copiedSpellSnapshot) {
        return new CopyCopiedSpellForTargetPlayerEffect(new StackEntry(copiedSpellSnapshot));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
