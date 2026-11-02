package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "206")
public class ShapersSanctuary extends Card {

    public ShapersSanctuary() {
        // Whenever a creature you control becomes the target of a spell or ability
        // an opponent controls, you may draw a card.
        addTrigger(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
