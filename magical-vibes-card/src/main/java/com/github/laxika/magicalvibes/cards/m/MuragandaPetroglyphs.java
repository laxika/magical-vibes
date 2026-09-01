package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNoAbilitiesPredicate;

@CardRegistration(set = "FUT", collectorNumber = "146")
public class MuragandaPetroglyphs extends Card {

    public MuragandaPetroglyphs() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ALL_CREATURES,
                new PermanentHasNoAbilitiesPredicate()));
    }
}
