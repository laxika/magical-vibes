package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "202")
public class ArchonOfValorsReach extends Card {

    public ArchonOfValorsReach() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardTypeOnEnterEffect(List.of(
                CardType.LAND,
                CardType.CREATURE,
                CardType.BATTLE,
                CardType.KINDRED,
                CardType.PLANE,
                CardType.PHENOMENON,
                CardType.SCHEME,
                CardType.CONSPIRACY,
                CardType.DUNGEON,
                CardType.VANGUARD
        )));
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsMatchingPredicateEffect(
                new CardHasSourceChosenCardTypePredicate()));
    }
}
