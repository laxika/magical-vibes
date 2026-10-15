package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "263")
public class HallOfTagsin extends Card {

    public HallOfTagsin() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {1}, {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}, {T}: Add one mana of any color."
        ));
        // {4}, {T}: Create a tapped Powerstone token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(CreateTokenEffect.ofPowerstoneToken(new Fixed(1))),
                "{4}, {T}: Create a tapped Powerstone token."
        ));
    }
}
