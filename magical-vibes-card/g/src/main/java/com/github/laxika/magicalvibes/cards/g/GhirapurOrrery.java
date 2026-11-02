package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "216")
@CardRegistration(set = "BLC", collectorNumber = "270")
public class GhirapurOrrery extends Card {

    public GhirapurOrrery() {
        addStaticEffect(new EachPlayerPlaysAdditionalLandEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ConditionalEffect(
                new ActivePlayerHandEmpty(), new DrawCardForTargetPlayerEffect(3)));
    }
}
