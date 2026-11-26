package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MIR", collectorNumber = "160")
public class BuildersBane extends Card {

    public BuildersBane() {
        targetExactlyX(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Targets must be artifacts"
        ), Integer.MAX_VALUE).addSpellEffect(DestroyTargetPermanentEffect.eachTarget());

        addSpellEffect(DealDamageEffect.to(Subjects.eachPlayer(),
                AmountThisWay.died(StepKind.DESTROY).controlledByYou()));
    }
}
