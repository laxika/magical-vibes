package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "214")
public class PyromancersGauntlet extends Card {

    public PyromancersGauntlet() {
        // If a red instant or sorcery spell you control or a red planeswalker you control would
        // deal damage to a permanent or player, it deals that much damage plus 2 instead.
        addEffect(EffectSlot.STATIC, new AdditionalControllerDamageEffect(2,
                new StackEntryAnyOfPredicate(List.of(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryColorInPredicate(Set.of(CardColor.RED))
                        )),
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                                new StackEntryColorInPredicate(Set.of(CardColor.RED))
                        ))
                ))
        ));
    }
}
