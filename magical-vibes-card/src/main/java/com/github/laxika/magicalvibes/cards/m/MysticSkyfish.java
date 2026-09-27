package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M21", collectorNumber = "326")
public class MysticSkyfish extends Card {

    public MysticSkyfish() {
        // Whenever you draw your second card each turn, this creature gains flying until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
