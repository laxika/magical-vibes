package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "39")
@CardRegistration(set = "OTC", collectorNumber = "75")
public class LeylineDowser extends Card {

    public LeylineDowser() {
        // {1}, {T}: Mill a card. You may put an instant or sorcery card milled this way into your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MillControllerAndMayReturnMilledPermanentToHandEffect(
                        1,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))),
                "{1}, {T}: Mill a card. You may put an instant or sorcery card milled this way into your hand."
        ));

        // Tap an untapped legendary creature you control: Untap this artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                        ))),
                        new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Tap an untapped legendary creature you control: Untap Leyline Dowser."
        ));
    }
}
