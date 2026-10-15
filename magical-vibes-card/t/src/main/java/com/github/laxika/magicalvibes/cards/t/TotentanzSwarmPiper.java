package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "216")
public class TotentanzSwarmPiper extends Card {

    private static CreateTokenEffect ratToken() {
        return new CreateTokenEffect(
                1,
                "Rat",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.RAT),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBlockEffect()));
    }

    public TotentanzSwarmPiper() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, ratToken());
        addEffect(EffectSlot.ON_DEATH, ratToken());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{1}{B}: Target attacking Rat you control gains deathtouch until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.RAT),
                                new PermanentControlledBySourceControllerPredicate()
                        )),
                        "Target must be an attacking Rat you control"
                )
        ));
    }
}
