package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsAfraidOfController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "308")
@CardRegistration(set = "MB2", collectorNumber = "544")
public class CreepyCrawler extends Card {

    public CreepyCrawler() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ConditionalEffect(
                        new TargetPlayerIsAfraidOfController(),
                        SequenceEffect.of(
                                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                                new DrawCardEffect())));
    }
}
