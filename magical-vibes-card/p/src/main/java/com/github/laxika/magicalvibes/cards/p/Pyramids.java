package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveDamageFromTargetLandInsteadOfNextDestructionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ARN", collectorNumber = "67")
public class Pyramids extends Card {

    public Pyramids() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Destroy target Aura attached to a land.",
                                new DestroyTargetPermanentEffect(),
                                new PermanentPredicateTargetFilter(
                                        new PermanentIsAuraAttachedToLandPredicate(),
                                        "Target must be an Aura attached to a land")),
                        new ChooseOneEffect.ChooseOneOption(
                                "The next time target land would be destroyed this turn, remove all damage marked on it instead.",
                                new RemoveDamageFromTargetLandInsteadOfNextDestructionEffect(),
                                TargetFilters.land())
                ))),
                "{2}: Choose one — Destroy target Aura attached to a land. The next time target land would be destroyed this turn, remove all damage marked on it instead."
        ));
    }
}
