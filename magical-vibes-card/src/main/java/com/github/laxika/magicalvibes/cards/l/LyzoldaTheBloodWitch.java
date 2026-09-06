package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "117")
public class LyzoldaTheBloodWitch extends Card {

    public LyzoldaTheBloodWitch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(),
                        new ConditionalEffect(
                                new SacrificedCardMatches(new CardColorPredicate(CardColor.RED), "red"),
                                new DealDamageToAnyTargetEffect(2)),
                        new ConditionalEffect(
                                new SacrificedCardMatches(new CardColorPredicate(CardColor.BLACK), "black"),
                                new DrawCardEffect())
                ),
                "{2}, Sacrifice a creature: Lyzolda deals 2 damage to any target if the sacrificed creature was red. Draw a card if the sacrificed creature was black."
        ));
    }
}
