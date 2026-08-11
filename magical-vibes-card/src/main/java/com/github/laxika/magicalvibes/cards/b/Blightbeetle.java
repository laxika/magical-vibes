package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantHavePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "87")
public class Blightbeetle extends Card {

    public Blightbeetle() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantHavePlusOnePlusOneCountersEffect(), GrantScope.OPPONENT_CREATURES));
    }
}
