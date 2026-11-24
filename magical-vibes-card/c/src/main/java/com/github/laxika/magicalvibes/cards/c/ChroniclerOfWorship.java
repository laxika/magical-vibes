package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "25")
public class ChroniclerOfWorship extends Card {

    public ChroniclerOfWorship() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        // "Put a random Shrine card from among the top seven cards": a random pick, not a seek.
                        SeekLibraryEffect.randomWithoutSeeking(new Fixed(1), new CardSubtypePredicate(CardSubtype.SHRINE),
                                LibrarySearchDestination.HAND, null).fromAmongTop(7),
                        new PerpetuallyChangeCastCostEffect(Subjects.cardsThisWay(StepKind.SEEK), -1),
                        new ShuffleLibraryEffect(false)));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
