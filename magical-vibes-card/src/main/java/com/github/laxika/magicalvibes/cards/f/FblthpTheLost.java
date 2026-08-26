package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;

@CardRegistration(set = "WAR", collectorNumber = "50")
public class FblthpTheLost extends Card {

    public FblthpTheLost() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new EnteredFromZone(Zone.LIBRARY), new DrawCardEffect()));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new ShuffleSelfIntoOwnerLibraryEffect());
    }
}
