package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromAmongExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "238")
@CardRegistration(set = "DBL", collectorNumber = "505")
public class GrolnokTheOmnivore extends Card {

    public GrolnokTheOmnivore() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.FROG)),
                new MillEffect(3, Subjects.you()));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.PERMANENT_CARD, ObjectRelation.YOU_OWN),
                ExileGraveyardCardsEffect.cards(Subjects.triggeringCards()),
                PutCounterOnExiledCardsEffect.onCardsExiledThisWay(CounterType.CROAK));
        addStaticEffect(new PlayFromAmongExiledCardsEffect(Subjects.exiledCards(Subjects.you(),
                new CardHasCounterInExilePredicate(CounterType.CROAK))));
    }
}
