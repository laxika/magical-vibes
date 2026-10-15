package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;

import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "127")
public class WallOfPutridFlesh extends Card {

    public WallOfPutridFlesh() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addEffect(EffectSlot.STATIC,
                new PreventDamageToSelfFromCreaturesEffect(new PermanentIsEnchantedPredicate()));
    }
}
