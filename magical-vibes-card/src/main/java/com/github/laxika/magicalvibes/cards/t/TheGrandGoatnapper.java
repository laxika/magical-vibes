package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "20")
public class TheGrandGoatnapper extends Card {

    public TheGrandGoatnapper() {
        // Spells you cast have affinity for Goats.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(),
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GOAT), CountScope.CONTROLLER),
                CostModificationScope.SELF));

        PermanentPredicate anotherNonGoatCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GOAT)),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.GOAT),
                        new ConjureCardNamedIntoHandEffect("Goatnap", false)
                ),
                "{T}: Another target non-Goat creature perpetually becomes a Goat in addition to its other types. "
                        + "Then conjure a card named Goatnap into your hand. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(anotherNonGoatCreature,
                        "Target must be another non-Goat creature"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
