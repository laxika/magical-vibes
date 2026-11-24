package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "189")
public class StormKingsThunder extends Card {

    public StormKingsThunder() {
        // "Copy that spell X times": X fixed as the delayed ability is made; none is made at X 0.
        addSpellEffect(new NoteAmountEffect(new XValue()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.atLeast(StepKind.NOTE, 1),
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(CardPredicateUtils.instantOrSorcery(),
                        List.of(new CopyTriggeringSpellEffect().withCopies(AmountThisWay.noted())))));
    }
}
