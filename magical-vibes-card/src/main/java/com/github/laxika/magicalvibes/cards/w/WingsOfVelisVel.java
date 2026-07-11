package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "LRW", collectorNumber = "97")
public class WingsOfVelisVel extends Card {

    public WingsOfVelisVel() {
        // Until end of turn, target creature has base power and toughness 4/4,
        // gains all creature types (changeling), and gains flying.
        addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(4, 4));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
