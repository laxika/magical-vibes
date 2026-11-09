package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "140")
@CardRegistration(set = "PIP", collectorNumber = "435")
@CardRegistration(set = "PIP", collectorNumber = "668")
@CardRegistration(set = "PIP", collectorNumber = "963")
public class PipBoy3000 extends Card {

    public PipBoy3000() {
        ChooseOneAtTriggerTimeEffect attackChoice = new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Sort Inventory — Draw a card, then discard a card",
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER))),
                new ChooseOneEffect.ChooseOneOption(
                        "Pick a Perk — Put a +1/+1 counter on that creature",
                        new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                                CounterType.PLUS_ONE_PLUS_ONE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Check Map — Untap up to two target lands",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                        TargetFilters.land(), null, 0, 2, false, null)
        )));
        addTrigger(Triggers.attacks(), attackChoice);
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
