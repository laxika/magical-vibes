package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "BLB", collectorNumber = "169")
public class CuriousForager extends Card {

    public CuriousForager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ForageEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsPermanentPredicate())
                        .targetGraveyard(true)
                        .build()),
                "Forage and return a target permanent card from your graveyard to your hand?"));
    }
}
