package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "234")
public class GorionWiseMentor extends Card {

    public GorionWiseMentor() {
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                CopyControllerCastSpellOnSpellCastEffect.adventureCopy(new CardTruePredicate()),
                "Copy that spell?"));
    }
}
