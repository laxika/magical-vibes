package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "252")
@CardRegistration(set = "INR", collectorNumber = "437")
@CardRegistration(set = "VOW", collectorNumber = "251")
@CardRegistration(set = "DBL", collectorNumber = "518")
public class WanderingMind extends Card {

    public WanderingMind() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6,
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
