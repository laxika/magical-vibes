package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForYouAndDamagedPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "175")
@CardRegistration(set = "C20", collectorNumber = "18")
public class XyrisTheWrithingStorm extends Card {

    public XyrisTheWrithingStorm() {
        addTrigger(Triggers.opponentDrawsCardExceptFirstInDrawStep(),
                new CreateTokenEffect(
                        "Snake", 1, 1, CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of()));
        EventValue damage = new EventValue();
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DrawCardForYouAndDamagedPlayerEffect(damage));
    }
}
