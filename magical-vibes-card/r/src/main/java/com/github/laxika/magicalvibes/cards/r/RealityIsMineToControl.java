package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "354")
public class RealityIsMineToControl extends Card {

    public RealityIsMineToControl() {
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                CopyControllerCastSpellOnSpellCastEffect.withSacrificeFilter(
                        null, new PermanentIsSourcePermanentPredicate(), "this scheme", true),
                "Abandon this scheme and copy that spell?"));
    }
}
