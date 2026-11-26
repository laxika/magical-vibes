package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "83")
@CardRegistration(set = "SLD", collectorNumber = "509")
public class DavrielRogueShadowmage extends Card {

    public DavrielRogueShadowmage() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerHandAtMost(1)),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 2));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DiscardEffect(1, Subjects.targetPlayer())),
                "−1: Target player discards a card."
        ));
    }
}
