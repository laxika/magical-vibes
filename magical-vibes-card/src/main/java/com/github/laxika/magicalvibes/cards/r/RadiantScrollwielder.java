package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;

@CardRegistration(set = "STX", collectorNumber = "221")
public class RadiantScrollwielder extends Card {

    public RadiantScrollwielder() {
        addEffect(EffectSlot.STATIC, GrantLifelinkToControllerSpellsByColorEffect.allColors());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect());
    }
}
