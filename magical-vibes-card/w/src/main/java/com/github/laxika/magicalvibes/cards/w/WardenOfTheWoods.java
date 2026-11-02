package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "213")
public class WardenOfTheWoods extends Card {

    public WardenOfTheWoods() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(new DrawCardEffect(2), "Draw two cards?"));
    }
}
