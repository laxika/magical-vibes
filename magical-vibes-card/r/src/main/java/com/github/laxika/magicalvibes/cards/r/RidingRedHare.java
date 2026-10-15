package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "PTK", collectorNumber = "18")
public class RidingRedHare extends Card {

    public RidingRedHare() {
        // Target creature gets +3/+3 and gains horsemanship until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HORSEMANSHIP, GrantScope.TARGET));
    }
}
