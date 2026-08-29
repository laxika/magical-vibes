package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "15")
public class BrunaTheFadingLight extends Card {

    public BrunaTheFadingLight() {
        // When you cast this spell, you may return target Angel or Human creature card from your
        // graveyard to the battlefield.
        addEffect(EffectSlot.ON_SELF_CAST, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ANGEL),
                                new CardSubtypePredicate(CardSubtype.HUMAN))))))
                .targetGraveyard(true)
                .upTo(true)
                .build());
    }
}
