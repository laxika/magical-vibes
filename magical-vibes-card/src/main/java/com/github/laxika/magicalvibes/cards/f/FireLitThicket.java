package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "271")
public class FireLitThicket extends Card {

    public FireLitThicket() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {R/G}, {T}: Add {R}{R}, {R}{G}, or {G}{G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R/G}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN), 2)),
                "{R/G}, {T}: Add {R}{R}, {R}{G}, or {G}{G}."
        ));
    }
}
