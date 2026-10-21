package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "55")
public class TritonCavalry extends Card {

    public TritonCavalry() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new SpellCastTriggerEffect(null,
                                List.of(ReturnToHandEffect.target(new PermanentIsEnchantmentPredicate())),
                                new StackEntryTargetsSourcePredicate()),
                        "Return target enchantment to its owner's hand?"));
    }
}
