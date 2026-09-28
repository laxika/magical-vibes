package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/** An effect that needs the copied spell snapshot when a spell-copy trigger is collected. */
public interface CopiedSpellReferencingEffect extends CardEffect {

    CardEffect snapshotCopiedSpell(StackEntry copiedSpellSnapshot);
}
