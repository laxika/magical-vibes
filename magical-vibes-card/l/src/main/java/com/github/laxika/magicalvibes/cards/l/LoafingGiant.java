package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INV", collectorNumber = "153")
public class LoafingGiant extends Card {

    public LoafingGiant() {
        var effect = new MillControllerThenIfMilledEffect(
                1,
                new CardTypePredicate(CardType.LAND),
                PreventDamageEffect.allCombatBySelf());
        addEffect(EffectSlot.ON_ATTACK, effect);
        addEffect(EffectSlot.ON_BLOCK, effect);
    }
}
