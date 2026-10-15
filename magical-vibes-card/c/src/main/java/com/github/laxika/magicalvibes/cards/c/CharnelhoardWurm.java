package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "CON", collectorNumber = "100")
@CardRegistration(set = "C13", collectorNumber = "180")
@CardRegistration(set = "C18", collectorNumber = "171")
public class CharnelhoardWurm extends Card {

    public CharnelhoardWurm() {
        addEffect(EffectSlot.ON_DAMAGE_TO_OPPONENT, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build(), "Return the targeted card to your hand?"));
    }
}
