package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "186")
public class FalcoSparaPactweaver extends Card {

    public FalcoSparaPactweaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)));
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(
                Set.of(CardType.CREATURE, CardType.ARTIFACT, CardType.ENCHANTMENT, CardType.INSTANT,
                        CardType.SORCERY, CardType.PLANESWALKER, CardType.BATTLE, CardType.KINDRED),
                List.of(new RemoveCountersFromControlledCreaturesCastingCost(1, CounterType.ANY))));
    }
}
