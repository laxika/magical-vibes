package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "14")
@CardRegistration(set = "ICE", collectorNumber = "11")
public class CaribouRange extends Card {

    public CaribouRange() {
        // Enchant land you control — grants the land "{W}{W}, {T}: Create a 0/1 white Caribou creature token."
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land you control"
        ))
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, "{W}{W}",
                                List.of(new CreateTokenEffect("Caribou", 0, 1, CardColor.WHITE,
                                        List.of(CardSubtype.CARIBOU), Set.of(), Set.of())),
                                "{W}{W}, {T}: Create a 0/1 white Caribou creature token."),
                        GrantScope.ENCHANTED_PERMANENT
                ));

        // Sacrifice a Caribou token: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsTokenPredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.CARIBOU)
                                )),
                                "Sacrifice a Caribou token",
                                false
                        ),
                        new GainLifeEffect(1)
                ),
                "Sacrifice a Caribou token: You gain 1 life."
        ));
    }
}
