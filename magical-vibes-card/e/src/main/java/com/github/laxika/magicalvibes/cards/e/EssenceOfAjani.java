package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutResolvingSpellIntoCommandZoneEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "277")
@CardRegistration(set = "MB2", collectorNumber = "513")
public class EssenceOfAjani extends Card {

    public EssenceOfAjani() {
        addEffect(EffectSlot.SPELL, new PutResolvingSpellIntoCommandZoneEffect());
        addEffect(EffectSlot.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new GainLifeEffect(1))));
    }
}
