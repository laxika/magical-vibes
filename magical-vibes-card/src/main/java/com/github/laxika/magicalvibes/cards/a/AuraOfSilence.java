package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;

import java.util.Set;

public class AuraOfSilence extends Card {

    public AuraOfSilence() {
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCastCostEffect(Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT), 2));
        addEffect(EffectSlot.ON_SACRIFICE, new DestroyTargetPermanentEffect(Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT)));
    }
}
