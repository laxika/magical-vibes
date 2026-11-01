package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryWithCollectionCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "184")
public class EvelynTheCovetous extends Card {

    public EvelynTheCovetous() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.VAMPIRE),
                        new ExileTopCardOfEachPlayersLibraryWithCollectionCounterEffect()));
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect.forCollectionCounters(true));
    }
}
