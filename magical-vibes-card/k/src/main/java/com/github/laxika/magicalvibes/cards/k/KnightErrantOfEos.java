package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.ConvokeCreatureCount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "26")
@CardRegistration(set = "MOM", collectorNumber = "346")
public class KnightErrantOfEos extends Card {

    public KnightErrantOfEos() {
        ConvokeCreatureCount convokeCount = new ConvokeCreatureCount();
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LookAtTopCardsEffect(
                new Fixed(6),
                new Fixed(2),
                new CardTypePredicate(CardType.CREATURE),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                false,
                LibrarySearchDestination.HAND,
                true,
                false,
                convokeCount),
                new ShuffleLibraryEffect(false));
    }
}
