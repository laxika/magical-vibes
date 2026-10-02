package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedAurasToLegalTargetsFollowUp;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;

@CardRegistration(set = "WOC", collectorNumber = "18")
@CardRegistration(set = "WOC", collectorNumber = "54")
public class KnickknackOuphe extends Card {

    public KnickknackOuphe() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LookAtTopCardsEffect(
                new XValue(), new XValue(), new CardIsAuraPredicate(),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true,
                LibrarySearchDestination.BATTLEFIELD, true, false, new XValue(),
                null, false, 0, false, false, false, false, 0,
                new AttachSelectedAurasToLegalTargetsFollowUp()));
    }
}
