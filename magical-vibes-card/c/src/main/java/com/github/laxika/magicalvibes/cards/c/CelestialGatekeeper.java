package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "6")
public class CelestialGatekeeper extends Card {

    public CelestialGatekeeper() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(),
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.BIRD),
                                        new CardSubtypePredicate(CardSubtype.CLERIC)
                                ))
                        )), 2, false, false)));
    }
}
