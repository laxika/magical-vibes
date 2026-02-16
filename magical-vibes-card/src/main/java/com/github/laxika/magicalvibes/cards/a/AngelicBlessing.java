package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;

public class AngelicBlessing extends Card {

    public AngelicBlessing() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));
        addEffect(EffectSlot.SPELL, new GrantKeywordToTargetEffect(Keyword.FLYING));
    }
}
