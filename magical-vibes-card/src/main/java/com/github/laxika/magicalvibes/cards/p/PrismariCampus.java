package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "270")
public class PrismariCampus extends Card {

    public PrismariCampus() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED))),
                "{T}: Add {U} or {R}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ScryEffect(1)),
                "{4}, {T}: Scry 1."
        ));
    }
}
