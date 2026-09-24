package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "455")
public class YidrisMaelstromWielder extends Card {

    public YidrisMaelstromWielder() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                RegisterDelayedControllerSpellCastTriggerEffect.withStackEntryFilter(
                        new StackEntryCastFromZonePredicate(Zone.HAND),
                        List.of(new CascadeEffect()),
                        false));
    }
}
