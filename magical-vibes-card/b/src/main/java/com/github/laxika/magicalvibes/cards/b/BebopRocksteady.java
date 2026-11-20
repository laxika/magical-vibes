package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "140")
@CardRegistration(set = "TMT", collectorNumber = "204")
@CardRegistration(set = "TMT", collectorNumber = "241")
public class BebopRocksteady extends Card {

    public BebopRocksteady() {
        // Whenever Bebop & Rocksteady attacks or blocks, sacrifice a permanent unless you discard a card.
        addTrigger(Triggers.attacks(), sacrificeOrDiscardEffect());
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, sacrificeOrDiscardEffect());
    }

    private ChooseOneAtResolutionEffect sacrificeOrDiscardEffect() {
        return new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Sacrifice a permanent",
                        new SacrificePermanentsEffect(1, new PermanentTruePredicate(), Subjects.you())),
                new ChooseOneEffect.ChooseOneOption("Discard a card",
                        new DiscardEffect(1, Subjects.you())))));
    }
}
