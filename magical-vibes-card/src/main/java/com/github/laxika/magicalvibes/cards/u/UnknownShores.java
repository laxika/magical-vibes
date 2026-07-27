package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "259")
public class UnknownShores extends Card {

    public UnknownShores() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}, {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}, {T}: Add one mana of any color."
        ));
    }
}
