package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "NCC", collectorNumber = "14")
@CardRegistration(set = "NCC", collectorNumber = "115")
public class ContractualSafeguard extends Card {

    public ContractualSafeguard() {
        addSpellEffect(new ConditionalEffect(
                new CastDuringMainPhase(),
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, Subjects.creaturesYouControl(), "a creature you control"),
                        new PutCounterOnTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE),
                                CounterType.SHIELD, 1))));
        addSpellEffect(new ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect());
    }
}
