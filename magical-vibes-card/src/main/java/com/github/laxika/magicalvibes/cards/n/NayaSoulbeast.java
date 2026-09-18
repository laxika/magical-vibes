package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardAndSetsTriggeringSpellXValueEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "C13", collectorNumber = "157")
public class NayaSoulbeast extends Card {

    public NayaSoulbeast() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new EachPlayerRevealsTopCardAndSetsTriggeringSpellXValueEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
