package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "27")
public class NeurokTransmuter extends Card {

    public NeurokTransmuter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT)),
                "{U}: Target creature becomes an artifact in addition to its other types until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new GrantColorUntilEndOfTurnEffect(CardColor.BLUE),
                        new RemoveCardTypeFromTargetPermanentEffect(CardType.ARTIFACT)
                ),
                "{U}: Until end of turn, target artifact creature becomes blue and isn't an artifact.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact creature"
                )
        ));
    }
}
