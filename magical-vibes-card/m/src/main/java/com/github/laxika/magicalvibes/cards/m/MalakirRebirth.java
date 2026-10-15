package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "111")
public class MalakirRebirth extends Card {

    public MalakirRebirth() {
        setBackFaceCard(new MalakirMire());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target creature. You lose 2 life. Until end of turn, that creature gains "
                                + "\"When this creature dies, return it to the battlefield tapped under its owner's control.\"",
                        List.of(
                                new LoseLifeEffect(2),
                                new GrantEffectToTargetUntilEndOfTurnEffect(
                                        EffectSlot.ON_DEATH,
                                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("Malakir Mire", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "MalakirMire";
    }
}
