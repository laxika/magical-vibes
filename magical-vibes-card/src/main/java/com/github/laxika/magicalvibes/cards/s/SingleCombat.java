package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "30")
public class SingleCombat extends Card {

    public SingleCombat() {
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesDownToCountEffect(
                1,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                ))
        ));
        addEffect(EffectSlot.SPELL, new PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect(
                Set.of(CardType.CREATURE, CardType.PLANESWALKER)
        ));
    }
}
