package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "154")
public class EtchedChampion extends Card {

    public EtchedChampion() {
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(
                new ProtectionFromColorsEffect(Set.of(
                        CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN
                ))
        ));
    }
}
