package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "166")
public class JayaBallardTaskMage extends Card {

    public JayaBallardTaskMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DiscardCardTypeCost(null, null), new DestroyTargetPermanentEffect()),
                "{R}, {T}, Discard a card: Destroy target blue permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                        "Target must be a blue permanent"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new DiscardCardTypeCost(null, null), new DealDamageToAnyTargetEffect(3, true)),
                "{1}{R}, {T}, Discard a card: Jaya Ballard deals 3 damage to any target. A creature dealt damage this way can't be regenerated this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{R}{R}",
                List.of(new DiscardCardTypeCost(null, null), new MassDamageEffect(6, true)),
                "{5}{R}{R}, {T}, Discard a card: Jaya Ballard deals 6 damage to each creature and each player."
        ));
    }
}
