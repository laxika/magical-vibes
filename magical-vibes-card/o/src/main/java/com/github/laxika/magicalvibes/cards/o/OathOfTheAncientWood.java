package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "187")
public class OathOfTheAncientWood extends Card {

    private static final String PROMPT = "Put a +1/+1 counter on target creature?";

    public OathOfTheAncientWood() {
        // Whenever this enchantment or another enchantment you control enters, you may put a
        // +1/+1 counter on target creature. The self half rides the ETB slot (target chosen as the
        // enchantment is cast); the ally half is queued as an enters-trigger target choice.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), PROMPT));

        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, new MayEffect(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), PROMPT));
    }
}
