package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "8")
@CardRegistration(set = "SIR", collectorNumber = "18")
@CardRegistration(set = "VOC", collectorNumber = "80")
public class BygoneBishop extends Card {

    public BygoneBishop() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.CREATURE))
                        .withStackEntryFilter(new StackEntryMaxManaValuePredicate(3)),
                CreateTokenEffect.ofClueToken(1));
    }
}
