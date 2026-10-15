package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageFromSourceToControllerEffect;

@CardRegistration(set = "SCG", collectorNumber = "95")
public class GoblinPsychopath extends Card {

    public GoblinPsychopath() {
        CardEffect effect = new FlipCoinWinEffect(null,
                new RedirectNextCombatDamageFromSourceToControllerEffect());
        addEffect(EffectSlot.ON_ATTACK, effect);
        addEffect(EffectSlot.ON_BLOCK, effect);
    }
}
