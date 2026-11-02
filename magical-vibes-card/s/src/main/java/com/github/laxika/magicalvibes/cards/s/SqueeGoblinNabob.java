package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "239")
@CardRegistration(set = "MMQ", collectorNumber = "214")
@CardRegistration(set = "MMA", collectorNumber = "130")
@CardRegistration(set = "UMA", collectorNumber = "150")
@CardRegistration(set = "SLC", collectorNumber = "1999")
@CardRegistration(set = "SOC", collectorNumber = "254")
@CardRegistration(set = "CMM", collectorNumber = "258")
@CardRegistration(set = "MB2", collectorNumber = "247")
@CardRegistration(set = "C19", collectorNumber = "152")
public class SqueeGoblinNabob extends Card {

    public SqueeGoblinNabob() {
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).filter(new CardIsSelfPredicate()).returnAll(true).build(), "Return Squee, Goblin Nabob from your graveyard to your hand?"));
    }
}
