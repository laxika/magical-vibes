package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;


@CardRegistration(set = "NEM", collectorNumber = "2")
public class AvengerEnDal extends Card {

    public AvengerEnDal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ExileTargetPermanentEffect(),
                        new GainLifeEffect(new TargetToughness(), Subjects.controllerOf(Subjects.target()))
                ),
                "{2}{W}, {T}, Discard a card: Exile target attacking creature. Its controller gains life equal to its toughness.",
                TargetFilters.attackingCreature()
        ));
    }
}
