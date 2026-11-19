package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RAV", collectorNumber = "277")
public class DuskmantleHouseOfShadow extends Card {

    public DuskmantleHouseOfShadow() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{B}",
                List.of(new MillEffect(1, Subjects.targetPlayer())),
                "{U}{B}, {T}: Target player mills a card."
        ));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
