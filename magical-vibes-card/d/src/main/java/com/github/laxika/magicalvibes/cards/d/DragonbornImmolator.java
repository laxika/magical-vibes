package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "51")
public class DragonbornImmolator extends Card {

    public DragonbornImmolator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}{R}: This creature gets +1/+0 until end of turn."
        ));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new SourcePowerAtLeast(1)),
                SequenceEffect.of(
                        new NoteAmountEffect(new EventValue()),
                        RegisterDelayedControllerSpellCastTriggerEffect.boon(new CardTypePredicate(CardType.CREATURE),
                                List.of(PerpetuallyBoostCardEffect.toCards(Subjects.cardsOf(Subjects.triggeringSpell()),
                                        AmountThisWay.noted(), new Fixed(0))))));
    }
}
