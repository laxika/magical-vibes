package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "145")
@CardRegistration(set = "KHM", collectorNumber = "297")
public class Quakebringer extends Card {

    public Quakebringer() {
        addStaticEffect(new OpponentsCantGainLifeEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withEventCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GIANT))),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addCastingOption(new ForetellCast("{2}{R}{R}"));
    }
}
