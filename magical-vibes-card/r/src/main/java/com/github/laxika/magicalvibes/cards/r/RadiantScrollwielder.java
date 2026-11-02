package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "221")
public class RadiantScrollwielder extends Card {

    public RadiantScrollwielder() {
        addStaticEffect(GrantLifelinkToControllerSpellsByColorEffect.allColors());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect());
    }
}
