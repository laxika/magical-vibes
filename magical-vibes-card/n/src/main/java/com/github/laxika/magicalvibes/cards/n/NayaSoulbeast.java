package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetTriggeringSpellXValueEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C13", collectorNumber = "157")
public class NayaSoulbeast extends Card {

    public NayaSoulbeast() {
        addTrigger(Triggers.youCastThisSpell(), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.eachPlayer()),
                new SetTriggeringSpellXValueEffect(AmountThisWay.revealedManaValue())));
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
