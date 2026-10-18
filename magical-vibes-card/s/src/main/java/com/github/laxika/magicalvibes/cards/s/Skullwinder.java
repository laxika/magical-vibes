package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C15", collectorNumber = "39")
@CardRegistration(set = "M3C", collectorNumber = "247")
@CardRegistration(set = "OTC", collectorNumber = "207")
@CardRegistration(set = "C20", collectorNumber = "189")
@CardRegistration(set = "CMA", collectorNumber = "146")
public class Skullwinder extends Card {

    public Skullwinder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                        .targetGraveyard(true)
                        .build(),
                new ChooseOpponentReturnsCardFromGraveyardToHandEffect(null)));
    }
}
