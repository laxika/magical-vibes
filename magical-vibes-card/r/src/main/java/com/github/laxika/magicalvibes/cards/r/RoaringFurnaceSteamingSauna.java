package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "230")
public class RoaringFurnaceSteamingSauna extends Card {

    public RoaringFurnaceSteamingSauna() {
        setRoomDoorManaCosts(List.of("{1}{R}", "{3}{U}{U}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Roaring Furnace", List.of())
                        .withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption("Steaming Sauna", List.of())
                        .withManaCost("{3}{U}{U}")
        )));

        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.doorUnlocked(0),
                new DealDamageToTargetCreatureEffect(new CardsInHand(CountScope.CONTROLLER), false,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(
                                        new PermanentControlledBySourceControllerPredicate())))));

        addStaticEffect(new ConditionalEffect(new SourceRoomDoorUnlocked(1),
                new NoMaximumHandSizeEffect()));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceRoomDoorUnlocked(1)),
                new DrawCardEffect(1));
    }
}
