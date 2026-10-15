package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEachTargetCreatureEqualToColorCountEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardMatchEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "2")
@CardRegistration(set = "DMC", collectorNumber = "50")
public class JaredCarthalion extends Card {

    private static final Set<CardColor> ALL_COLORS = Set.of(
            CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    public JaredCarthalion() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(1, "Kavu", 3, 3, null, ALL_COLORS,
                        List.of(CardSubtype.KAVU), Set.of(Keyword.TRAMPLE), Set.of())),
                "+1: Create a 3/3 Kavu creature token with trample that's all colors."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutCountersOnEachTargetCreatureEqualToColorCountEffect(
                        CounterType.PLUS_ONE_PLUS_ONE)),
                "\u22123: Choose up to two target creatures. For each of them, put a number of +1/+1 "
                        + "counters on it equal to the number of colors it is.",
                null, -3, null, null,
                List.<TargetFilter>of(TargetFilters.creature(), TargetFilters.creature()), 0, 2
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsMulticoloredPredicate())
                                .targetGraveyard(true)
                                .build(),
                        new RecordReturnedGraveyardCardMatchEffect(new CardHasExactlyNColorsPredicate(5)),
                        new ConditionalEffect(new EventValueAtLeast(1),
                                SequenceEffect.of(new DrawCardEffect(), CreateTokenEffect.ofTreasureToken(2)))
                ),
                "\u22126: Return target multicolored card from your graveyard to your hand. If that card was "
                        + "all colors, draw a card and create two Treasure tokens."
        ));
    }
}
