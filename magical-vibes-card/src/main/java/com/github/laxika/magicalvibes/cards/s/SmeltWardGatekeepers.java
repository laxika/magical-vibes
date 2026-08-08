package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "39")
public class SmeltWardGatekeepers extends Card {

    public SmeltWardGatekeepers() {
        // When this creature enters, if you control two or more Gates, gain control of target
        // creature an opponent controls until end of turn. Untap that creature. It gains haste
        // until end of turn. Intervening-if gate (CR 603.4): the Gate count is checked as the
        // trigger goes on the stack and again at resolution, so the target is chosen at trigger
        // time rather than at cast time.
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                        SequenceEffect.of(
                                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))));
    }
}
