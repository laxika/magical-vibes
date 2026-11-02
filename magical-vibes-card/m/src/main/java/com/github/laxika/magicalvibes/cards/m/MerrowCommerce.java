package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "72")
public class MerrowCommerce extends Card {

    public MerrowCommerce() {
        // At the beginning of your end step, untap all Merfolk you control.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)));
    }
}
