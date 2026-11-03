package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "110")
@CardRegistration(set = "PIO", collectorNumber = "150")
@CardRegistration(set = "NCC", collectorNumber = "272")
@CardRegistration(set = "AFC", collectorNumber = "135")
@CardRegistration(set = "C20", collectorNumber = "157")
@CardRegistration(set = "BLC", collectorNumber = "199")
@CardRegistration(set = "FDC", collectorNumber = "169")
@CardRegistration(set = "C17", collectorNumber = "139")
public class OutpostSiege extends Card {

    private static final String KHANS = "Khans";
    private static final String DRAGONS = "Dragons";

    public OutpostSiege() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of(KHANS, DRAGONS)));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceHasChosenMode(KHANS)),
                new ExileTopCardMayPlayThisTurnEffect(1, false));

        addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceHasChosenMode(DRAGONS)),
                new DealDamageToAnyTargetEffect(1));
    }
}
