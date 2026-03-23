package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "183")
@CardRegistration(set = "M11", collectorNumber = "175")
public class GarrukWildspeaker extends Card {

    public GarrukWildspeaker() {
        // +1: Untap two target lands.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapAllTargetPermanentsEffect()),
                "+1: Untap two target lands.",
                null, 1, null, null,
                List.of(
                        new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "Target must be a land"),
                        new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "Target must be a land")
                ),
                2, 2
        ));

        // −1: Create a 3/3 green Beast creature token.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenEffect("Beast", 3, 3,
                        CardColor.GREEN, List.of(CardSubtype.BEAST),
                        Set.of(), Set.of())),
                "\u22121: Create a 3/3 green Beast creature token."
        ));

        // −4: Creatures you control get +3/+3 and gain trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(
                        new BoostAllOwnCreaturesEffect(3, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                ),
                "\u22124: Creatures you control get +3/+3 and gain trample until end of turn."
        ));
    }
}
