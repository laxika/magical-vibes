package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "22")
@CardRegistration(set = "OPCA", collectorNumber = "43")
@CardRegistration(set = "MOC", collectorNumber = "149")
public class KharashaFoothills extends Card {

    public KharashaFoothills() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentIsAttackingOpponentOfSourceControllerPredicate()),
                new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect());
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                SacrificePermanentsEffect.anyNumber(new PermanentIsCreaturePredicate(), Subjects.you()),
                new ConditionalEffect(
                        HappenedThisWay.sacrificed(),
                        DealDamageEffect.toTargetCreature(new AmountThisWay(StepKind.SACRIFICE)))));
    }
}
