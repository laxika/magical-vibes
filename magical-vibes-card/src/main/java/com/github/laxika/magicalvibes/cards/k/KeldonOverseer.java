package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "134")
public class KeldonOverseer extends Card {

    public KeldonOverseer() {
        // Kicker {3}{R}
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}{R}"));

        // When this creature enters, if it was kicked, gain control of target creature
        // until end of turn. Untap that creature. It gains haste until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(), 
                new GainControlOfTargetPermanentUntilEndOfTurnEffect()
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(), 
                new UntapPermanentsEffect(TapUntapScope.TARGET)
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(), 
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
        ));
    }
}
