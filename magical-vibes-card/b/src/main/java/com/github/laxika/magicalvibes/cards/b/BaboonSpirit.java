package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "89")
@CardRegistration(set = "TLE", collectorNumber = "177")
public class BaboonSpirit extends Card {

    public BaboonSpirit() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        spiritToken()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(FlickerEffect.exileTargetReturnAtEndStep()),
                "{3}{U}: Exile another target creature you control. Return it to the battlefield under its owner's control at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature you control"
                )
        ));
    }

    private static CreateTokenEffect spiritToken() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Spirit",
                1,
                1,
                null,
                null,
                List.of(CardSubtype.SPIRIT),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new CantBeBlockedByCreaturesMatchingPredicateEffect(
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))),
                        new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT), "Spirits")
                )),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        );
    }
}
