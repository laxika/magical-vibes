package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "178")
public class RuggedPrairie extends Card {

    public RuggedPrairie() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {R/W}, {T}: Add {R}{R}, {R}{W}, or {W}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R/W}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE), 2)),
                "{R/W}, {T}: Add {R}{R}, {R}{W}, or {W}{W}."
        ));
    }
}
