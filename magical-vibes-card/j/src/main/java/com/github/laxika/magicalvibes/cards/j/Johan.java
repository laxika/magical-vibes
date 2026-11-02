package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.JohanCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "77")
@CardRegistration(set = "LEG", collectorNumber = "236")
public class Johan extends Card {

    public Johan() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new MayEffect(new JohanCombatEffect(),
                        "Have Johan not attack so your creatures don't tap this combat?"));
    }
}
