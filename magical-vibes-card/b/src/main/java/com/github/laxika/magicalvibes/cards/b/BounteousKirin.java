package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "123")
public class BounteousKirin extends Card {

    public BounteousKirin() {
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new GainLifeEqualToCastSpellManaValueEffect(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)))),
                "Gain life equal to that spell's mana value?"));
    }
}
