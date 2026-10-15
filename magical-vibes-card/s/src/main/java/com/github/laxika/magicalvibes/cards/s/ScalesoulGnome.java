package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "28")
public class ScalesoulGnome extends Card {

    public ScalesoulGnome() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DiscoverEffect(new EventValue()));

        List<CardEffect> duplicate =
                List.of(new ConjureDuplicateOfTriggeringCardIntoHandEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, duplicate,
                        new StackEntryCastFromZonePredicate(Zone.EXILE)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new LandPlayFromExileTriggerEffect(duplicate));
    }
}
