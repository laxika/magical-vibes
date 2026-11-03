package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "247")
@CardRegistration(set = "SLD", collectorNumber = "2496")
@CardRegistration(set = "AFC", collectorNumber = "2")
public class ProsperTomeBound extends Card {

    public ProsperTomeBound() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));

        List<CardEffect> treasure = List.of(CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                treasure.toArray(CardEffect[]::new));
        addTrigger(Triggers.youPlayLand(),
                new LandPlayFromExileTriggerEffect(treasure));
    }
}
