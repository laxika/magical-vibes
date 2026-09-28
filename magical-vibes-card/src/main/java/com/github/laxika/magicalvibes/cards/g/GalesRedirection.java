package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellAndGrantCastPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

@CardRegistration(set = "HBG", collectorNumber = "119")
public class GalesRedirection extends Card {

    public GalesRedirection() {
        addEffect(EffectSlot.SPELL, new RollD20Effect(
                null,
                new ExileTargetSpellAndGrantCastPermissionEffect(false),
                new ExileTargetSpellAndGrantCastPermissionEffect(true),
                null,
                new Scaled(new TargetSpellManaValue(), -1),
                14,
                false));
    }
}
