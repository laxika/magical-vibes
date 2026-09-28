package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "15")
@CardRegistration(set = "LTC", collectorNumber = "99")
public class GwaihirGreatestOfTheEagles extends Card {

    public GwaihirGreatestOfTheEagles() {
        PermanentPredicateTargetFilter attackingCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate()
                )),
                "Target must be an attacking creature");

        // Whenever Gwaihir attacks, target attacking creature gains flying until end of turn.
        target(attackingCreature).addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));

        // At the beginning of each end step, if you gained 3 or more life this turn, create a 3/3
        // white Bird creature token with flying and the same attack trigger.
        CardEffect birdAttackTrigger = new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET);
        CreateTokenEffect birdToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Bird", 3, 3,
                CardColor.WHITE, null,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_ATTACK, birdAttackTrigger), List.of(),
                false, false, false, 0, Set.of())
                .withTokenTargetFilter(attackingCreature);
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(3), birdToken));
    }
}
