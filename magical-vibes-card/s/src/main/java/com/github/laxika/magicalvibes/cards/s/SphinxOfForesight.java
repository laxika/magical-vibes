package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOpeningHandUpkeepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "55")
public class SphinxOfForesight extends Card {

    public SphinxOfForesight() {
        addStaticEffect(new MayEffect(
                new RegisterOpeningHandUpkeepTriggerEffect(new ScryEffect(3), true),
                "Reveal this card from your opening hand?"
        ));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(1));
    }
}
