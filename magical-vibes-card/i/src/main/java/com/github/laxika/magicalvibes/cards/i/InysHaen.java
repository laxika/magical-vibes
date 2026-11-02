package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "54")
public class InysHaen extends Card {

    public InysHaen() {
        MillEffect millThree = new MillEffect(3, MillRecipient.CONTROLLER);
        addTrigger(Triggers.planeswalkToThis(), millThree);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), millThree);

        addTrigger(Triggers.planeswalkAwayFromThis(),
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        Integer.MAX_VALUE,
                        new CardTypePredicate(CardType.LAND),
                        null,
                        false,
                        true));

        addTrigger(Triggers.chaosEnsues(), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))))
                .targetGraveyard(true)
                .build());
    }
}
