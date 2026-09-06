package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "54")
public class ShieldmageElder extends Card {

    public ShieldmageElder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.CLERIC)),
                        PreventDamageEffect.allByTargetCreatures()),
                "Tap two untapped Clerics you control: Prevent all damage target creature would deal this turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                        new PreventDamageFromTargetSpellEffect(false, false)),
                "Tap two untapped Wizards you control: Prevent all damage target spell would deal this turn."));
    }
}
