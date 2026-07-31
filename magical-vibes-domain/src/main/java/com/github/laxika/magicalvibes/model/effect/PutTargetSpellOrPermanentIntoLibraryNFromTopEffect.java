package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Puts target spell or nonland permanent into its owner's library at a specific position from the
 * top. Position is 0-indexed: 0 = top, 1 = second from top, 2 = third from top, etc.
 *
 * <p>Like {@link ChangeColorTextEffect} / {@link SetTargetColorEffect}, the permanent target is
 * described by {@link #targetSpec()} ({@code PERMANENT}, nonland); the spell capability is exposed
 * through {@code EffectResolution.targetsSpellOnStack(effect)} and validated on the stack path.
 * Moving a spell off the stack is <em>not</em> countering it — uncounterable spells are still
 * removed (Commit // Memory).
 *
 * @param position the 0-indexed position from the top of the library
 */
public record PutTargetSpellOrPermanentIntoLibraryNFromTopEffect(int position) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT,
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }
}
