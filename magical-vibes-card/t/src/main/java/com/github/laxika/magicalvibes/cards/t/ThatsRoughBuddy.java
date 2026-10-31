package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.CreatureLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "87")
public class ThatsRoughBuddy extends Card {

    public ThatsRoughBuddy() {
        target(TargetFilters.creature())
                .addSpellEffect(new ConditionalEffect(
                        new CreatureLeftBattlefieldUnderYourControlThisTurn(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)))
                .addSpellEffect(new ConditionalEffect(
                        new NotCondition(new CreatureLeftBattlefieldUnderYourControlThisTurn()),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
