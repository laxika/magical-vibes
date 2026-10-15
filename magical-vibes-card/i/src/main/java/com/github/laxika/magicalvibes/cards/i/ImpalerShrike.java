package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

@CardRegistration(set = "NPH", collectorNumber = "36")
public class ImpalerShrike extends Card {

    public ImpalerShrike() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new SacrificeSelfThenEffect(new DrawCardEffect(3)),
                        "You may sacrifice it. If you do, draw three cards."));
    }
}
