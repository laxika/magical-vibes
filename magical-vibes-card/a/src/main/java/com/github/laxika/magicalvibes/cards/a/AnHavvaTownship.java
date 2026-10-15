package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "111")
public class AnHavvaTownship extends Card {

    public AnHavvaTownship() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {1}, {T}: Add {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{1}, {T}: Add {G}."
        ));
        // {2}, {T}: Add {R} or {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))),
                "{2}, {T}: Add {R} or {W}."
        ));
    }
}
