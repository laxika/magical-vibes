package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "38")
public class ConductiveCurrent extends Card {

    public ConductiveCurrent() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));
        addEffect(EffectSlot.SPELL, new ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect(
                new CardAllOfPredicate(List.of(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))))), 2));
    }
}
