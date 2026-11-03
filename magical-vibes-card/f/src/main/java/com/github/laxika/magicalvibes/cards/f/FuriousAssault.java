package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "191")
public class FuriousAssault extends Card {

    public FuriousAssault() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
    }
}
