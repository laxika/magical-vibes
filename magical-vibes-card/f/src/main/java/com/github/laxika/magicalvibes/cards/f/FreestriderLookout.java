package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "163")
public class FreestriderLookout extends Card {

    public FreestriderLookout() {
        addTrigger(Triggers.youCommitCrime(),
                new OncePerTurnTriggerEffect(
                        LookAtTopCardsEffect.mayPutOneMatchingOntoBattlefieldRestOnBottomRandom(
                                5, new CardTypePredicate(CardType.LAND))));
    }
}
