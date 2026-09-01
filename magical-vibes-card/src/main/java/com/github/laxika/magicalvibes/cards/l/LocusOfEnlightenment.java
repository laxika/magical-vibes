package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;

public class LocusOfEnlightenment extends Card {

    public LocusOfEnlightenment() {
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfExiledCardsEffect(true));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(null));
    }
}
