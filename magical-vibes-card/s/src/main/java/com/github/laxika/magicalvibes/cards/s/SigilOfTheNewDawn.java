package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "55")
@CardRegistration(set = "UMA", collectorNumber = "35")
public class SigilOfTheNewDawn extends Card {

    public SigilOfTheNewDawn() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                UnlessPaysEffect.youMayPay("{1}{W}", new PutCardsIntoHandEffect(Subjects.triggeringCards()))
                        .withPrompt("Pay {1}{W} to return that card to your hand?"));
    }
}
