package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "271")
public class QuandrixCampus extends Card {

    public QuandrixCampus() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ScryEffect(1)),
                "{4}, {T}: Scry 1."
        ));
    }
}
