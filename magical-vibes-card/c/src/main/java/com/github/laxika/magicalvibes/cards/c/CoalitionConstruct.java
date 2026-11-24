package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "30")
public class CoalitionConstruct extends Card {

    public CoalitionConstruct() {
        // As Coalition Construct enters, choose a creature type.
        addStaticEffect(new ChooseSubtypeOnEnterEffect());

        // Coalition Construct is the chosen type in addition to its other types.
        addStaticEffect(GrantChosenSubtypeToOwnCreaturesEffect.toSelf());

        // Other creatures you control of the chosen type and creature cards of that type in your
        // hand perpetually get +1/+1.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new PerpetuallyBoostTargetCreatureEffect(Subjects.controlledBy(Subjects.you(),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSourceChosenSubtypePredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                                1, 1),
                        PerpetuallyBoostCardEffect.toCards(Subjects.cardsInYourHand(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardHasSourceChosenSubtypePredicate()))), 1, 1)));
    }
}
