package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "322")
public class PhyrexianTower extends Card {

    public PhyrexianTower() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Sacrifice a creature: Add {B}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(), new AwardManaEffect(ManaColor.BLACK, 2)),
                "{T}, Sacrifice a creature: Add {B}{B}."
        ));
    }
}
