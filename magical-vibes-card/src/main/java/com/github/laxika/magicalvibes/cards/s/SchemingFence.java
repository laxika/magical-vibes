package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;

@CardRegistration(set = "SNC", collectorNumber = "219")
public class SchemingFence extends Card {

    public SchemingFence() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseNonlandPermanentOnEnterEffect());
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
                new PermanentIsChosenPermanentPredicate()));
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfChosenPermanentEffect());
        addEffect(EffectSlot.STATIC, new SpendManaAsAnyColorForActivatedAbilitiesEffect());
    }
}
