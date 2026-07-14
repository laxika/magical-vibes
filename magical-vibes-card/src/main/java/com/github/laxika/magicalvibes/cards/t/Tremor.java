package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "6ED", collectorNumber = "211")
@CardRegistration(set = "P02", collectorNumber = "118")
@CardRegistration(set = "7ED", collectorNumber = "225")
@CardRegistration(set = "8ED", collectorNumber = "228")
public class Tremor extends Card {

    public Tremor() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, false, false, new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
