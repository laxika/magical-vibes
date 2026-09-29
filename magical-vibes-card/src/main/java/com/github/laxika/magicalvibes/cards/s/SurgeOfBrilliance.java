package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastFromOutsideHandThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "WHO", collectorNumber = "57")
public class SurgeOfBrilliance extends Card {

    public SurgeOfBrilliance() {
        addEffect(EffectSlot.SPELL,
                new DrawCardEffect(new SpellsCastFromOutsideHandThisTurn(CountScope.CONTROLLER)));
        addCastingOption(new ForetellCast("{1}{U}"));
    }
}
