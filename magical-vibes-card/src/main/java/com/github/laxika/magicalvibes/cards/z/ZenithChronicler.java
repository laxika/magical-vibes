package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardsForEachOpponentOfTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstMulticoloredSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "246")
public class ZenithChronicler extends Card {

    public ZenithChronicler() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new FirstMulticoloredSpellCastTriggerEffect(
                        List.of(new DrawCardsForEachOpponentOfTriggeringPlayerEffect(1))));
    }
}
