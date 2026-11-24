package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "62")
@CardRegistration(set = "DDJ", collectorNumber = "10")
public class Galvanoth extends Card {

    public Galvanoth() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                LookAtTopCardsAndMayCastEffect.lookAt(1)
                        .matching(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .leavingTheRest(),
                "Look at the top card of your library?"
        ));
    }
}
