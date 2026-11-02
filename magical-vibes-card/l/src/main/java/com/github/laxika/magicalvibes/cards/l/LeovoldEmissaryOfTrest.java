package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCanDrawOnlyOneCardEachTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UMA", collectorNumber = "202")
public class LeovoldEmissaryOfTrest extends Card {

    public LeovoldEmissaryOfTrest() {
        addStaticEffect(new OpponentsCanDrawOnlyOneCardEachTurnEffect());
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
