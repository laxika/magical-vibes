package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "60")
public class Mindwhisker extends Card {

    public Mindwhisker() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SurveilEffect(1));
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, new CardTruePredicate()),
                new StaticBoostEffect(-1, 0, GrantScope.OPPONENT_CREATURES)));
    }
}
