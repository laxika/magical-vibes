package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "137")
public class FireServant extends Card {

    public FireServant() {
        addEffect(EffectSlot.STATIC, new DoubleControllerDamageEffect(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryColorInPredicate(Set.of(CardColor.RED))
                )),
                false
        ));
    }
}
