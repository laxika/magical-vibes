package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneOfEachCardTypeFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "72")
@CardRegistration(set = "NCC", collectorNumber = "172")
public class GrimeGorger extends Card {

    public GrimeGorger() {
        addTrigger(Triggers.attacks(), new ExileUpToOneOfEachCardTypeFromGraveyardEffect());
    }
}
