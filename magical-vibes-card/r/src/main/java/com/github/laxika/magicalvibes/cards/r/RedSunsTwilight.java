package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "145")
public class RedSunsTwilight extends Card {

    public RedSunsTwilight() {
        targetX(TargetFilters.artifact(), 100)
                .addSpellEffect(new DestroyTargetPermanentEffect(Subjects.allTargets(), new PermanentIsArtifactPredicate()));
        // "If X is 5 or more, for each artifact destroyed this way, create a token that's a copy of it. Those tokens
        // gain haste. Exile them at the beginning of the next end step."
        addSpellEffect(new ConditionalEffect(new SpellXAtLeast(5),
                new CreateTokenCopyOfTargetPermanentEffect(true, true)
                        .withSubject(Subjects.departedThisWay(StepKind.DESTROY))));
    }
}
