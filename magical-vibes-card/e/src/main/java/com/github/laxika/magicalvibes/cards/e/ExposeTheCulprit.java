package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAndCloakDisguisedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetFaceUpEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "124")
@CardRegistration(set = "MKM", collectorNumber = "307")
public class ExposeTheCulprit extends Card {

    public ExposeTheCulprit() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Turn target face-down creature face up",
                        new TurnTargetFaceUpEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile any number of face-up creatures you control with disguise in a face-down pile, shuffle that pile, then cloak them",
                        new ExileAndCloakDisguisedCreaturesEffect())
        )));
    }
}
