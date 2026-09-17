package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "FCA", collectorNumber = "56")
public class KraumLudevicsOpus extends Card {

    public KraumLudevicsOpus() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new NthSpellCastTriggerEffect(2, List.of(new DrawCardEffect())));
    }
}
