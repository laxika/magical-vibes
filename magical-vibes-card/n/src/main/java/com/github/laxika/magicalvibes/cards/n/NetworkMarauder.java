package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "8")
public class NetworkMarauder extends Card {

    private static final CardAllOfPredicate ARTIFACT_CREATURE = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.ARTIFACT),
            new CardTypePredicate(CardType.CREATURE)));

    private static final CardAnyOfPredicate BOOSTED_CARDS = new CardAnyOfPredicate(List.of(
            ARTIFACT_CREATURE,
            new CardSubtypePredicate(CardSubtype.SPACECRAFT)));

    private static final PerpetuallyBoostOwnedCardsEffect BOOST =
            new PerpetuallyBoostOwnedCardsEffect(BOOSTED_CARDS, 1, 1);

    public NetworkMarauder() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), BOOST);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ARTIFACT,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentMinManaValuePredicate(3))))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                BOOST);
    }
}
