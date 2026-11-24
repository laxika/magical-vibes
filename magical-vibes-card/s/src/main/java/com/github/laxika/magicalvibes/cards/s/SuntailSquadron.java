package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;

@CardRegistration(set = "YMID", collectorNumber = "11")
public class SuntailSquadron extends Card {

    public SuntailSquadron() {
        addSpellEffect(ConjureCardsEffect.named("JUD", "28", ConjureDestination.intoHand())
                .repeatingWhileHandSmallerThan(new Fixed(7)));
    }
}
