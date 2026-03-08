package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "NPH", collectorNumber = "123")
public class TriumphOfTheHordes extends Card {

    public TriumphOfTheHordes() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INFECT, GrantScope.OWN_CREATURES));
    }
}
