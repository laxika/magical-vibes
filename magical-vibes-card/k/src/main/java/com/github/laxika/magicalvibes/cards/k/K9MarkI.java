package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "47")
@CardRegistration(set = "WHO", collectorNumber = "362")
@CardRegistration(set = "WHO", collectorNumber = "537")
@CardRegistration(set = "WHO", collectorNumber = "652")
@CardRegistration(set = "WHO", collectorNumber = "953")
@CardRegistration(set = "WHO", collectorNumber = "1128")
public class K9MarkI extends Card {

    public K9MarkI() {
        PermanentHasSupertypePredicate legendary = new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);

        // As long as K-9 is untapped, other legendary creatures you control have ward {1}.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(1),
                        GrantScope.OWN_CREATURES,
                        legendary)));

        // {1}{U}, {T}: Target legendary creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{1}{U}, {T}: Target legendary creature can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                legendary)),
                        "Target must be a legendary creature")));
    }
}
