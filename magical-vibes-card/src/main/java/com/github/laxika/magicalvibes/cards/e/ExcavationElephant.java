package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DOM", collectorNumber = "17")
public class ExcavationElephant extends Card {

    public ExcavationElephant() {
        // Kicker {1}{W}
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{W}"));

        // When this creature enters, if it was kicked, return target artifact card
        // from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.ARTIFACT))
                        .build()
        ));
    }
}
