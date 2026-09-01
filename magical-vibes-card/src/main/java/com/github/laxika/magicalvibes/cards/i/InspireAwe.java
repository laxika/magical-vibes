package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "175")
public class InspireAwe extends Card {

    public InspireAwe() {
        // Enchanted creatures and enchantment creatures are exempt from the combat-damage prevention.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsEnchantedPredicate(),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()))))));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
