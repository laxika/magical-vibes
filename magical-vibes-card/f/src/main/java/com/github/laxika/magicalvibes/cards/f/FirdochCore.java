package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "255")
public class FirdochCore extends Card {

    public FirdochCore() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new AnimatePermanentsEffect(4, 4, List.of(), Set.of())),
                "{4}: This artifact becomes a 4/4 artifact creature until end of turn."
        ));
    }
}
