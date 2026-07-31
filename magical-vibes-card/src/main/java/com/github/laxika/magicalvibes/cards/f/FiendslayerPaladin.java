package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "18")
public class FiendslayerPaladin extends Card {

    public FiendslayerPaladin() {
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.fromOpponentSpellColors(
                Set.of(CardColor.BLACK, CardColor.RED)));
    }
}
