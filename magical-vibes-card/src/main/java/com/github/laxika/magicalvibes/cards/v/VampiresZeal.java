package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetIfSubtypeEffect;

@CardRegistration(set = "XLN", collectorNumber = "43")
public class VampiresZeal extends Card {

    public VampiresZeal() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SPELL, new GrantKeywordToTargetIfSubtypeEffect(Keyword.FIRST_STRIKE, CardSubtype.VAMPIRE));
    }
}
