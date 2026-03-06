package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "35")
public class GitaxianProbe extends Card {

    public GitaxianProbe() {
        addEffect(EffectSlot.SPELL, new LookAtHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
