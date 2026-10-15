package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "DSC", collectorNumber = "354")
public class RealityIsMineToControl extends Card {

    public RealityIsMineToControl() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                CopyControllerCastSpellOnSpellCastEffect.withSacrificeFilter(
                        null, new PermanentIsSourcePermanentPredicate(), "this scheme", true),
                "Abandon this scheme and copy that spell?"));
    }
}
