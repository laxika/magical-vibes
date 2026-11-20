package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes creatures in the selected subject every creature type. The own-creatures
 * mode also applies to that controller's creature spells and creature cards they own outside the
 * battlefield; the self mode applies to the source card in every zone. The optional filter narrows
 * the affected battlefield permanents.
 */
public record GrantAllCreatureTypesToOwnCreaturesEffect(PermanentSubject subject, PermanentPredicate filter)
        implements CardEffect {

    public GrantAllCreatureTypesToOwnCreaturesEffect() {
        this(Subjects.otherCreaturesYouControl(), null);
    }

    public GrantAllCreatureTypesToOwnCreaturesEffect(PermanentSubject subject) {
        this(subject, null);
    }

    public static GrantAllCreatureTypesToOwnCreaturesEffect toSelf() {
        return new GrantAllCreatureTypesToOwnCreaturesEffect(Subjects.source(), null);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
