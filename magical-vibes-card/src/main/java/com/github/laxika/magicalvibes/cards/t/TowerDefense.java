package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "GTC", collectorNumber = "137")
public class TowerDefense extends Card {

    public TowerDefense() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(0, 5));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.REACH, GrantScope.OWN_CREATURES));
    }
}
