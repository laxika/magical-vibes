package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantVentureIntoDungeonMoreThanOnceEachTurnEffect;

@CardRegistration(set = "AFR", collectorNumber = "22")
public class KeenEaredSentry extends Card {

    public KeenEaredSentry() {
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        addEffect(EffectSlot.STATIC, new OpponentsCantVentureIntoDungeonMoreThanOnceEachTurnEffect());
    }
}
