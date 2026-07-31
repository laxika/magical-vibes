package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "37")
public class StormElemental extends Card {

    public StormElemental() {
        // {U}, Exile the top card of your library: Tap target creature with flying.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new ExileTopCardOfLibraryCost(1), new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{U}, Exile the top card of your library: Tap target creature with flying.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.FLYING)
                        )),
                        "Target must be a creature with flying"
                )
        ));

        // {U}, Exile the top card of your library: If the exiled card is a snow land, this
        // creature gets +1/+1 until end of turn. The exile cost imprints what it exiled so the
        // effect can inspect it at resolution.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new ExileTopCardOfLibraryCost(1, true),
                        new ConditionalEffect(
                                new ImprintedCardMatches(
                                        new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.LAND),
                                                new CardSupertypePredicate(CardSupertype.SNOW)
                                        )),
                                        "a snow land"
                                ),
                                new BoostSelfEffect(1, 1)
                        )
                ),
                "{U}, Exile the top card of your library: If the exiled card is a snow land, "
                        + "this creature gets +1/+1 until end of turn."
        ));
    }
}
