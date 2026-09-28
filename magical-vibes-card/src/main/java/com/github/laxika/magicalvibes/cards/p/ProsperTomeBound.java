package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "247")
@CardRegistration(set = "SLD", collectorNumber = "2496")
@CardRegistration(set = "AFC", collectorNumber = "2")
public class ProsperTomeBound extends Card {

    public ProsperTomeBound() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));

        List<CardEffect> treasure = List.of(CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, treasure,
                        new StackEntryCastFromZonePredicate(Zone.EXILE)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new LandPlayFromExileTriggerEffect(treasure));
    }
}
