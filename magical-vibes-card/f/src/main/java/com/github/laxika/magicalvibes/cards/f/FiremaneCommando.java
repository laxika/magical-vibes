package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "145")
@CardRegistration(set = "MOC", collectorNumber = "73")
@CardRegistration(set = "MOC", collectorNumber = "81")
@CardRegistration(set = "FDC", collectorNumber = "31")
public class FiremaneCommando extends Card {

    public FiremaneCommando() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(2)),
                new DrawCardEffect());
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(
                        new AllOf(List.of(new MinimumAttackers(2), new AttackedTargetIsOpponent()))),
                new ConditionalEffect(new NotCondition(new OpponentAttacksWithAtLeastCreatures(1, false)), new DrawCardForTargetPlayerEffect(1)));
    }
}
