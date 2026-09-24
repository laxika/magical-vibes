package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "139")
@CardRegistration(set = "MSC", collectorNumber = "317")
public class MonologueTax extends Card {

    public MonologueTax() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new NthSpellCastTriggerEffect(2, List.of(CreateTokenEffect.ofTreasureToken(1))));
    }
}
