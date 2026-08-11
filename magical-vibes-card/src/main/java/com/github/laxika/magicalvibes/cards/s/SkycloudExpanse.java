package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "327")
public class SkycloudExpanse extends Card {

    public SkycloudExpanse() {
        // {1}, {T}: Add {W}{U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE)
                ),
                "{1}, {T}: Add {W}{U}."
        ));
    }
}
