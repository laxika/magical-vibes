package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromOpponentSourcesToControllerAndCommanderRetaliationEffect;

@CardRegistration(set = "FIC", collectorNumber = "455")
public class JudgmentOfAlexander extends Card {

    public JudgmentOfAlexander() {
        addEffect(EffectSlot.SPELL,
                new PreventAllDamageFromOpponentSourcesToControllerAndCommanderRetaliationEffect());
    }
}
