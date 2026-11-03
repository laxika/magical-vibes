package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "40")
@CardRegistration(set = "DDU", collectorNumber = "39")
@CardRegistration(set = "2XM", collectorNumber = "65")
@CardRegistration(set = "NEC", collectorNumber = "96")
public class Riddlesmith extends Card {

    public Riddlesmith() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                new MayEffect(SequenceEffect.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)), "Draw a card and discard a card?"));
    }
}
