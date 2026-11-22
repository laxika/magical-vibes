package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "123")
public class LostInTheWoods extends Card {

    public LostInTheWoods() {
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, new CardSubtypePredicate(CardSubtype.FOREST)),
                        RemoveTargetFromCombatEffect.ofTriggeringPermanent()),
                PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()));
    }
}
