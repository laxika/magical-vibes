package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "166")
public class SpikefieldHazard extends Card {

    public SpikefieldHazard() {
        setBackFaceCard(new SpikefieldCave());
        setModalDoubleFaced(true);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Spikefield Hazard",
                        List.of(new DealDamageToAnyTargetEffect(new Fixed(1)),
                                new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(
                                        Subjects.thisWay(StepKind.DEAL_DAMAGE)))),
                new ChooseOneEffect.ChooseOneOption("Spikefield Cave", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpikefieldCave";
    }
}
