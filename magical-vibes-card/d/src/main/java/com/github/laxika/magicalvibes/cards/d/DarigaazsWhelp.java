package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "8")
public class DarigaazsWhelp extends Card {

    public DarigaazsWhelp() {
        addStaticEffect(new KickerEffect("{1}{R}"));
        addTrigger(Triggers.youDrawCard().withFilter(new CardSubtypePredicate(CardSubtype.DRAGON)),
                PerpetuallyBoostCardEffect.ofTriggeringCard(1, 1));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                SequenceEffect.of(
                        new SeekLibraryEffect(1, new CardSubtypePredicate(CardSubtype.DRAGON)),
                        new PerpetuallyBoostCardEffect(this, 1, 1),
                        PerpetuallyBoostCardEffect.toCards(Subjects.cardsThisWay(StepKind.SEEK), 1, 1)));
    }
}
