package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "192")
public class AjaniSleeperAgent extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever you cast a creature or planeswalker spell, target opponent gets two poison counters.";

    public AjaniSleeperAgent() {
        CardAnyOfPredicate creatureOrPlaneswalker = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.PLANESWALKER)
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RevealTopCardMatchingToHandElseMayBottomEffect(creatureOrPlaneswalker)),
                "+1: Reveal the top card of your library. If it's a creature or planeswalker card, put it into your hand. Otherwise, you may put it on the bottom of your library."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        DistributeCountersAmongTargetsEffect.chosenAmongAnyNumberOfTargetCreatures(
                                CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3), null),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)
                ),
                "\u22123: Distribute three +1/+1 counters among up to three target creatures. They gain vigilance until end of turn.",
                null,
                -3,
                null,
                null,
                List.of(TargetFilters.creature(), TargetFilters.creature(), TargetFilters.creature()),
                0,
                3
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(
                                creatureOrPlaneswalker,
                                List.of(new GivePoisonCountersEffect(2, PoisonRecipient.EACH_OPPONENT))
                        )),
                        EMBLEM_TEXT
                )),
                "\u22126: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
