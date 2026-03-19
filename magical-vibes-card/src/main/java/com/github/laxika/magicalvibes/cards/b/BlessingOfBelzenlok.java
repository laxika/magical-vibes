package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetIfSupertypeEffect;

@CardRegistration(set = "DOM", collectorNumber = "77")
public class BlessingOfBelzenlok extends Card {

    public BlessingOfBelzenlok() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 1));
        addEffect(EffectSlot.SPELL, new GrantKeywordToTargetIfSupertypeEffect(Keyword.LIFELINK, CardSupertype.LEGENDARY));
    }
}
