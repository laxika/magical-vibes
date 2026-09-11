package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "134")
public class AkoumWarrior extends Card {

    public AkoumWarrior() {
        setBackFaceCard(new AkoumTeeth());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Akoum Warrior", List.of()),
                new ChooseOneEffect.ChooseOneOption("Akoum Teeth", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "AkoumTeeth";
    }
}
