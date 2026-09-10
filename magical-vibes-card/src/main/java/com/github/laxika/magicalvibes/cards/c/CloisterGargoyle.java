package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "7")
public class CloisterGargoyle extends Card {

    public CloisterGargoyle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCompletedDungeon(),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCompletedDungeon(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
