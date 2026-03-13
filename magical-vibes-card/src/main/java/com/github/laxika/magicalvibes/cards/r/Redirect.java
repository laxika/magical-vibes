package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "M11", collectorNumber = "71")
public class Redirect extends Card {

    public Redirect() {
        addEffect(EffectSlot.SPELL, new MayEffect(new ChooseNewTargetsForTargetSpellEffect(), "Choose new targets?"));
    }
}
