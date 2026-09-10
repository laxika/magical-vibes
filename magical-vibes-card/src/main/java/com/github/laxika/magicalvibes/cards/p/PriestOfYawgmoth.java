package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "19")
public class PriestOfYawgmoth extends Card {

    public PriestOfYawgmoth() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "an artifact", false, false, true, false),
                        new AwardManaEffect(ManaColor.BLACK, new XValue())),
                "{T}, Sacrifice an artifact: Add an amount of {B} equal to the sacrificed artifact's mana value."
        ));
    }
}
