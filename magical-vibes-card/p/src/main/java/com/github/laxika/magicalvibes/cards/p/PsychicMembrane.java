package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "46")
public class PsychicMembrane extends Card {

    public PsychicMembrane() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
