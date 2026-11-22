package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "247")
@CardRegistration(set = "C13", collectorNumber = "146")
public class Foster extends Card {

    public Foster() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new MayPayManaEffect("{1}",
                        SequenceEffect.of(
                                new RevealCardsFromTopUntilEffect(creatureCard),
                                new PutCardsIntoHandEffect(Subjects.revealedThisWay(creatureCard)),
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay())),
                        "Pay {1} to reveal cards until you reveal a creature card?"));
    }
}
