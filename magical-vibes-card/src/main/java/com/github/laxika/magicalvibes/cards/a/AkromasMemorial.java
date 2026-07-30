package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "200")
public class AkromasMemorial extends Card {

    public AkromasMemorial() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE),
                GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED)),
                GrantScope.OWN_CREATURES));
    }
}
