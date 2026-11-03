package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "18")
@CardRegistration(set = "FIC", collectorNumber = "133")
public class ElenaTurkRecruit extends Card {

    public ElenaTurkRecruit() {
        // When Elena enters, return target non-Assassin historic card from your graveyard to your hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardIsHistoricPredicate(),
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ASSASSIN)))))
                .targetGraveyard(true)
                .build());

        // Whenever you cast a historic spell, put a +1/+1 counter on Elena.
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
