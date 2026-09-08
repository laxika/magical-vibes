package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "149")
public class KioraTheCrashingWave extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your end step, create a 9/9 blue Kraken creature token.";

    public KioraTheCrashingWave() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(PreventDamageEffect.allToAndByTargetPermanentUntilNextTurn()),
                "+1: Until your next turn, prevent all damage that would be dealt to and dealt by target permanent an opponent controls.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        "Target must be a permanent an opponent controls"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DrawCardEffect(1), new PlayAdditionalLandsEffect(1)),
                "−1: Draw a card. You may play an additional land this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.END_STEP,
                                List.of(new CreateTokenEffect(
                                        "Kraken", 9, 9, CardColor.BLUE,
                                        List.of(CardSubtype.KRAKEN), Set.of(), Set.of())),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−5: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
