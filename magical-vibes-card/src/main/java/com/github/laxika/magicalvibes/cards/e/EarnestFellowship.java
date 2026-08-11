package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "21")
public class EarnestFellowship extends Card {

    public EarnestFellowship() {
        addEffect(EffectSlot.STATIC, protectionFromOwnColor(CardColor.WHITE));
        addEffect(EffectSlot.STATIC, protectionFromOwnColor(CardColor.BLUE));
        addEffect(EffectSlot.STATIC, protectionFromOwnColor(CardColor.BLACK));
        addEffect(EffectSlot.STATIC, protectionFromOwnColor(CardColor.RED));
        addEffect(EffectSlot.STATIC, protectionFromOwnColor(CardColor.GREEN));
    }

    private GrantEffectEffect protectionFromOwnColor(CardColor color) {
        return new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(color)),
                GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(color)));
    }
}
