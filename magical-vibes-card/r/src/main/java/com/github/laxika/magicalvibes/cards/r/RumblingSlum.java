package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "126")
@CardRegistration(set = "HOP", collectorNumber = "93")
@CardRegistration(set = "GK2", collectorNumber = "96")
public class RumblingSlum extends Card {

    public RumblingSlum() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), DealDamageEffect.to(Subjects.eachPlayer(), 1));
    }
}
