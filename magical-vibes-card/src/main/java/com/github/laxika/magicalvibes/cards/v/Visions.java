package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

@CardRegistration(set = "4ED", collectorNumber = "54")
public class Visions extends Card {

    public Visions() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsOfTargetLibraryEffect(5, TargetLibraryAction.MAY_SHUFFLE));
    }
}
