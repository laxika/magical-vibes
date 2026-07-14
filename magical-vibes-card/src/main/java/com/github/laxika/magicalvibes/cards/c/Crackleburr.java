package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "100")
public class Crackleburr extends Card {

    public Crackleburr() {
        // {U/R}{U/R}, {T}, Tap two untapped red creatures you control: This creature deals 3 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U/R}{U/R}",
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.RED)))), true),
                        new DealDamageToAnyTargetEffect(3, false)),
                "{U/R}{U/R}, {T}, Tap two untapped red creatures you control: This creature deals 3 damage to any target."));

        // {U/R}{U/R}, {Q}, Untap two tapped blue creatures you control: Return target creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U/R}{U/R}",
                List.of(
                        new UntapMultiplePermanentsCost(2, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE)))), true),
                        ReturnToHandEffect.target()),
                "{U/R}{U/R}, {Q}, Untap two tapped blue creatures you control: Return target creature to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")).withRequiresUntap());
    }
}
