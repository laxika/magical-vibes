package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "3")
public class AngelicBlessing extends Card {

    public AngelicBlessing() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FLYING, Scope.TARGET));
    }
}
