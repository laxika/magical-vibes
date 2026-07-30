package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "113")
public class CastleSengir extends Card {

    public CastleSengir() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {1}, {T}: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{1}, {T}: Add {B}."
        ));
        // {2}, {T}: Add {U} or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED))),
                "{2}, {T}: Add {U} or {R}."
        ));
    }
}
