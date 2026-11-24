package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "14")
public class CloneCrafter extends Card {

    public CloneCrafter() {
        // "Conjure a duplicate of a random creature card from your opponent's library into your hand. It perpetually
        // gains "You may spend mana as though it were mana of any color to cast this spell.""
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ChooseCardEffect.atRandom(Subjects.cardsInLibraries(Subjects.eachOpponent(),
                        new CardTypePredicate(CardType.CREATURE)), "a creature card"),
                ConjureCardsEffect.duplicateOf(Subjects.cardsThisWay(StepKind.CHOOSE), ConjureDestination.intoHand()),
                new PerpetuallyGrantAnyColorManaToCastEffect(Subjects.cardsThisWay(StepKind.CONJURE))));
    }
}
