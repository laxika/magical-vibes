package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BOK", collectorNumber = "73")
public class MarkOfTheOni extends Card {

    public MarkOfTheOni() {
        // Enchant creature. You control enchanted creature.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());

        // At the beginning of the end step, if you control no Demons, sacrifice this Aura.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NoOtherPermanent(new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                new SacrificeSelfEffect()));
    }
}
