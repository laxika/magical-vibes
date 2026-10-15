package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "12")
public class Dawnfluke extends Card {

    public Dawnfluke() {
        // Flash is auto-loaded from Scryfall keywords.
        // Evoke {W}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}"))));

        // When this creature enters, prevent the next 3 damage that would be dealt to any target this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, PreventDamageEffect.nextToTarget(3));

        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
