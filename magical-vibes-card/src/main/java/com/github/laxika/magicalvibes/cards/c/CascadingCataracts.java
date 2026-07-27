package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "240")
public class CascadingCataracts extends Card {

    public CascadingCataracts() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {5}, {T}: Add five mana in any combination of colors.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new AwardManaOfColorsEffect(
                        List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN),
                        5)),
                "{5}, {T}: Add five mana in any combination of colors."
        ));
    }
}
