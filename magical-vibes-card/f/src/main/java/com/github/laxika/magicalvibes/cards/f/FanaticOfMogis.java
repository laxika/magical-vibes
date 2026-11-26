package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "121")
@CardRegistration(set = "PIO", collectorNumber = "295")
public class FanaticOfMogis extends Card {

    public FanaticOfMogis() {
        // When this creature enters, it deals damage to each opponent equal to your red devotion.
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.to(Subjects.eachOpponent(),
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED)));
    }
}
