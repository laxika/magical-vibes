package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "180")
public class HuntersAmbush extends Card {

    public HuntersAmbush() {
        // Prevent all combat damage that would be dealt by nongreen creatures this turn.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
