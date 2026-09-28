package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "HBG", collectorNumber = "234")
public class GorionWiseMentor extends Card {

    public GorionWiseMentor() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                CopyControllerCastSpellOnSpellCastEffect.adventureCopy(new CardTruePredicate()),
                "Copy that spell?"));
    }
}
