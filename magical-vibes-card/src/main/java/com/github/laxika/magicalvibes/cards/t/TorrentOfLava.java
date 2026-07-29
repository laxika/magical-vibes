package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MIR", collectorNumber = "199")
public class TorrentOfLava extends Card {

    public TorrentOfLava() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, false, new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
