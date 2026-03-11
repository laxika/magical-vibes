package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M11", collectorNumber = "133")
public class DestructiveForce extends Card {

    public DestructiveForce() {
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesPermanentsEffect(5, new PermanentIsLandPredicate()));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(5));
    }
}
