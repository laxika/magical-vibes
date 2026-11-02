package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "164")
public class DrossScorpion extends Card {

    public DrossScorpion() {
        var untapTargetArtifact = new MayEffect(
                new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsArtifactPredicate()),
                "Untap target artifact?");

        // Whenever another artifact creature dies, you may untap target artifact.
        target(TargetFilters.artifact())
                .addTrigger(Triggers.creatureDies(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate())),
                                ObjectRelation.THIS_OR_ANOTHER),
                        untapTargetArtifact);
    }
}
