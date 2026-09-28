package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardValueEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedGraveyardCardValue;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "41")
@CardRegistration(set = "LTC", collectorNumber = "124")
public class MirkwoodElk extends Card {

    public MirkwoodElk() {
        addTriggerEffects(EffectSlot.ON_ENTER_BATTLEFIELD);
        addTriggerEffects(EffectSlot.ON_ATTACK);
    }

    private void addTriggerEffects(EffectSlot slot) {
        addEffect(slot, SequenceEffect.of(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardSubtypePredicate(CardSubtype.ELF))
                        .targetGraveyard(true)
                        .build(),
                new RecordReturnedGraveyardCardValueEffect(ReturnedGraveyardCardValue.POWER),
                new GainLifeEffect(new EventValue())));
    }
}
