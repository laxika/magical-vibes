package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TSP", collectorNumber = "178")
public class SubterraneanShambler extends Card {

    public SubterraneanShambler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(
                1,
                false,
                false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new MassDamageEffect(
                1,
                false,
                false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{3}{R}"));
    }
}
