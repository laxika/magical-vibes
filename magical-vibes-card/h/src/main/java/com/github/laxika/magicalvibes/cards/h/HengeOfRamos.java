package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "318")
public class HengeOfRamos extends Card {

    public HengeOfRamos() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}, {T}: Add one mana of any color."
        ));
    }
}
