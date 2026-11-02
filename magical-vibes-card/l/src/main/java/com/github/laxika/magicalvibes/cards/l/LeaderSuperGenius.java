package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawBeforeConniveReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "64")
public class LeaderSuperGenius extends Card {

    public LeaderSuperGenius() {
        addStaticEffect(new DrawBeforeConniveReplacementEffect());

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new DrawDiscardAndConniveEffect(true));
    }
}
