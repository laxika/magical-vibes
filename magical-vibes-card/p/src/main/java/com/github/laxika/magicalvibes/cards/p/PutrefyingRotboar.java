package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "10")
public class PutrefyingRotboar extends Card {

    public PutrefyingRotboar() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.BOAR)),
                new PerpetuallyGrantAbilitiesEffect(
                        Subjects.cardsInHands(Subjects.defendingPlayer(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                        Triggers.youCastThisSpell(), new LoseLifeEffect(1)));
    }
}
