package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "145")
@CardRegistration(set = "KHM", collectorNumber = "297")
public class Quakebringer extends Card {

    public Quakebringer() {
        addStaticEffect(new PlayersCantGainLifeEffect(Subjects.eachOpponent()));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                DealDamageEffect.to(Subjects.eachOpponent(), 2));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withEventCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GIANT))),
                DealDamageEffect.to(Subjects.eachOpponent(), 2));
        addCastingOption(new ForetellCast("{2}{R}{R}"));
    }
}
