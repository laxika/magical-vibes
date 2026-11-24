package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "16")
public class HardenedBonds extends Card {

    public HardenedBonds() {
        addTrigger(Triggers.plusOneCountersPutOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                SequenceEffect.of(
                        new SeekLibraryEffect(1, new CardTypePredicate(CardType.CREATURE)),
                        PerpetuallyBoostCardEffect.toCards(Subjects.cardsThisWay(StepKind.SEEK), 1, 1)));
    }
}
