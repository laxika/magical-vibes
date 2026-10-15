package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "3")
public class AjaniAdversaryOfTyrants extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your end step, create three 1/1 white Cat creature tokens with lifelink.";

    public AjaniAdversaryOfTyrants() {
        // +1: Put a +1/+1 counter on each of up to two target creatures. Two independent creature
        // target slots (0–2); the counter effect applies to every chosen target.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "+1: Put a +1/+1 counter on each of up to two target creatures.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature(), TargetFilters.creature()), 0, 2));

        // −2: Return target creature card with mana value 2 or less from your graveyard to the
        // battlefield.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(2))))
                        .targetGraveyard(true)
                        .build()),
                "−2: Return target creature card with mana value 2 or less from your graveyard to the "
                        + "battlefield."
        ));

        // −7: The emblem's end-step trigger is fired by StepTriggerService on its controller's turn.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(EmblemTriggerStep.END_STEP,
                                List.of(new CreateTokenEffect(3, "Cat", 1, 1, CardColor.WHITE,
                                        List.of(CardSubtype.CAT), Set.of(Keyword.LIFELINK), Set.of())),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
