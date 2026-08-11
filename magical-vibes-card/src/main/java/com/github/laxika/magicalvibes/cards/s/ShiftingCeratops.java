package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "194")
public class ShiftingCeratops extends Card {

    public ShiftingCeratops() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLUE)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new GrantChosenKeywordEffect(
                        List.of(Keyword.REACH, Keyword.TRAMPLE, Keyword.HASTE),
                        GrantScope.SELF
                )),
                "{G}: This creature gains your choice of reach, trample, or haste until end of turn."
        ));
    }
}
