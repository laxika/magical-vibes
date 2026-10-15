package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "168")
public class AerialVolley extends Card {

    public AerialVolley() {
        // Aerial Volley deals 3 damage divided as you choose among one, two, or three
        // target creatures with flying.
        target(new PermanentPredicateTargetFilter(
                flyingCreature(),
                "Target must be a creature with flying."
        ), 1, 3).addEffect(EffectSlot.SPELL, new DealDividedDamageEffect(
                new Fixed(3),
                null,
                DivisionMode.CHOSEN,
                flyingCreature(),
                0,
                false,
                false,
                false
        ));
    }

    private static PermanentAllOfPredicate flyingCreature() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));
    }
}
