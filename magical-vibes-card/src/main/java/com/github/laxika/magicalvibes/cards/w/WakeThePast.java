package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "PIP", collectorNumber = "221")
@CardRegistration(set = "PIP", collectorNumber = "479")
@CardRegistration(set = "PIP", collectorNumber = "749")
@CardRegistration(set = "PIP", collectorNumber = "1007")
@CardRegistration(set = "C21", collectorNumber = "75")
public class WakeThePast extends Card {

    public WakeThePast() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .returnAll(true)
                .grantHaste(true)
                .build());
    }
}
