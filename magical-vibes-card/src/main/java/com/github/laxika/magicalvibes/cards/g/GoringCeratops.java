package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "13")
public class GoringCeratops extends Card {

    public GoringCeratops() {
        // Whenever Goring Ceratops attacks, other creatures you control gain double strike until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES
        ));
    }
}
