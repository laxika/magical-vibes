package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "PC2", collectorNumber = "107")
public class VelaTheNightClad extends Card {

    public VelaTheNightClad() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.OWN_CREATURES));

        LoseLifeEffect loseLife = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, loseLife);
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD, loseLife);
    }
}
