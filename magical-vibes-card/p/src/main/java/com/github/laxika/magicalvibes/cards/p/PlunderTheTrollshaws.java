package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOB", collectorNumber = "51")
public class PlunderTheTrollshaws extends Card {

    public PlunderTheTrollshaws() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new DrawCardEffect(1),
                new DrawCardEffect(2)
        ));
        addCastingOption(new FlashbackCast("{3}{U}"));
    }
}
