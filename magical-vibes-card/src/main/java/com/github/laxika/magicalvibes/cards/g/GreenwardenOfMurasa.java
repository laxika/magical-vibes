package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "BFZ", collectorNumber = "174")
public class GreenwardenOfMurasa extends Card {

    public GreenwardenOfMurasa() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build(), "Return the targeted card from your graveyard to your hand?"));
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                SequenceEffect.of(
                        new ExileSourceCardFromGraveyardEffect(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .targetGraveyard(true)
                                .build()),
                "Exile Greenwarden of Murasa and return the targeted card from your graveyard to your hand?"));
    }
}
