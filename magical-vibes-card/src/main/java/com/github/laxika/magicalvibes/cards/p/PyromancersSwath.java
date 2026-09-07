package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "104")
public class PyromancersSwath extends Card {

    public PyromancersSwath() {
        addEffect(EffectSlot.STATIC, new AdditionalControllerDamageEffect(2,
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DiscardHandEffect());
    }
}
