package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "188")
public class FixWhatsBroken extends Card {

    public FixWhatsBroken() {
        addEffect(EffectSlot.SPELL, new PayXLifeCost());
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE))),
                Integer.MAX_VALUE,
                true));
    }
}
