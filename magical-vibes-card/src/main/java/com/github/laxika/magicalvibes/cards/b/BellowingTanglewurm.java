package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnCreaturesByColorEffect;

@CardRegistration(set = "SOM", collectorNumber = "111")
public class BellowingTanglewurm extends Card {

    public BellowingTanglewurm() {
        addEffect(EffectSlot.STATIC, new GrantKeywordToOwnCreaturesByColorEffect(CardColor.GREEN, Keyword.INTIMIDATE));
    }
}
