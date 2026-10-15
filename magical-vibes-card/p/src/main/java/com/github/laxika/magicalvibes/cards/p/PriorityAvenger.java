package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastInstantsUnlessSpellOrAbilityOnStackEffect;

@CardRegistration(set = "MB1", collectorNumber = "9")
public class PriorityAvenger extends Card {

    public PriorityAvenger() {
        addEffect(EffectSlot.STATIC, new PlayersCantCastInstantsUnlessSpellOrAbilityOnStackEffect());
    }
}
