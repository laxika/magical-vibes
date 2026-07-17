package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "204")
public class TitanicUltimatum extends Card {

    public TitanicUltimatum() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(5, 5));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.LIFELINK), GrantScope.OWN_CREATURES));
    }
}
