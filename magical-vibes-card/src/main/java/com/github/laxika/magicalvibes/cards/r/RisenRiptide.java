package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "73")
public class RisenRiptide extends Card {

    public RisenRiptide() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new KickedSpellCastTriggerEffect(
                List.of(new SetBasePowerToughnessEffect(5, 5, GrantScope.SELF))));
    }
}
