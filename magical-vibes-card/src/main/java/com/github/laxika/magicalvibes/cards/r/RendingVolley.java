package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "150")
public class RendingVolley extends Card {

    public RendingVolley() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                4, new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE))));
    }
}
