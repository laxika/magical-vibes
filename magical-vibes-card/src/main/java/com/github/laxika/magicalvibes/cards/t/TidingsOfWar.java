package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;

@CardRegistration(set = "HOB", collectorNumber = "115")
public class TidingsOfWar extends Card {

    public TidingsOfWar() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new AmassGoblinsEffect(1),
                new AmassGoblinsEffect(3)));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
