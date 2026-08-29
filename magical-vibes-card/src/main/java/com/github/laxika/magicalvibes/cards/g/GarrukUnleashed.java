package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "183")
public class GarrukUnleashed extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your end step, you may search your library for a creature card, "
                    + "put it onto the battlefield, then shuffle.";

    public GarrukUnleashed() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new BoostTargetCreatureEffect(3, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "+1: Up to one target creature gets +3/+3 and gains trample until end of turn.",
                null, +1, null, null,
                List.of(TargetFilters.creature()), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new CreateTokenEffect("Beast", 3, 3,
                                CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()),
                        new ConditionalEffect(
                                new OpponentControlsMoreCreatures(1),
                                new PutCounterOnEachMatchingPermanentEffect(
                                        CounterType.LOYALTY, 1,
                                        new PermanentIsSourceCardPredicate(),
                                        EachPermanentScope.ALL_PLAYERS))
                ),
                "−2: Create a 3/3 green Beast creature token. Then if an opponent controls more creatures than you, put a loyalty counter on Garruk."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.END_STEP,
                                List.of(new MayEffect(
                                        new SearchLibraryEffect(
                                                new CardTypePredicate(CardType.CREATURE),
                                                LibrarySearchDestination.BATTLEFIELD),
                                        "Search your library for a creature card?")),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\""));
    }
}
