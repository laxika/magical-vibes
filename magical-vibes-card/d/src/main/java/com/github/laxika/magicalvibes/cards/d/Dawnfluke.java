package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "12")
public class Dawnfluke extends Card {

    public Dawnfluke() {
        // Flash is auto-loaded from Scryfall keywords.
        // Evoke {W}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}"))));
        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());

        // When this creature enters, prevent the next 3 damage that would be dealt to any target this turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), PreventDamageEffect.nextToTarget(3));
    }
}
