package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CardTypesAmongControlledPermanentsAndGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.ColorsAmongControlledPermanentsAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "16")
@CardRegistration(set = "ELD", collectorNumber = "337")
@CardRegistration(set = "SLD", collectorNumber = "1828")
public class HappilyEverAfter extends Card {

    public HappilyEverAfter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainLifeEffect(new Fixed(5), GainLifeRecipient.EACH_PLAYER),
                new EachPlayerDrawsCardEffect(1));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new ColorsAmongControlledPermanentsAtLeast(5),
                                new CardTypesAmongControlledPermanentsAndGraveyardAtLeast(6),
                                new ControllerLifeAtLeast(0, true)))),
                new WinGameEffect());
    }
}
