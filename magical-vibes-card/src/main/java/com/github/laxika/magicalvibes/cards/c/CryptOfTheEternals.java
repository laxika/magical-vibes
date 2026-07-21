package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "169")
public class CryptOfTheEternals extends Card {

    public CryptOfTheEternals() {
        // When this land enters, you gain 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {1}, {T}: Add {U}, {B}, or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED))),
                "{1}, {T}: Add {U}, {B}, or {R}."
        ));
    }
}
