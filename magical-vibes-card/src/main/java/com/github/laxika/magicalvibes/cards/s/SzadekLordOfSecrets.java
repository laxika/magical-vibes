package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

@CardRegistration(set = "RAV", collectorNumber = "234")
public class SzadekLordOfSecrets extends Card {

    public SzadekLordOfSecrets() {
        addEffect(EffectSlot.STATIC, new ReplaceCombatDamageWithMillEffect(
                new PermanentIsSourceCardPredicate(),
                CounterType.PLUS_ONE_PLUS_ONE
        ));
    }
}
