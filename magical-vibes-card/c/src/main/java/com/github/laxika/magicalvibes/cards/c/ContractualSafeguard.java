package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "NCC", collectorNumber = "14")
@CardRegistration(set = "NCC", collectorNumber = "115")
public class ContractualSafeguard extends Card {

    public ContractualSafeguard() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastDuringMainPhase(),
                new PutCounterOnChosenOwnPermanentEffect(
                        CounterType.SHIELD, 1, new PermanentIsCreaturePredicate())));
        addEffect(EffectSlot.SPELL,
                new ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect());
    }
}
