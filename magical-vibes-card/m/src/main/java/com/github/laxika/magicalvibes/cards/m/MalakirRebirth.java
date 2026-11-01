package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "111")
public class MalakirRebirth extends Card {

    public MalakirRebirth() {
        setBackFaceCard(new MalakirMire());
        setModalDoubleFaced(true);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target creature. You lose 2 life. Until end of turn, that creature gains "
                                + "\"When this creature dies, return it to the battlefield tapped under its owner's control.\"",
                        List.of(
                                new LoseLifeEffect(2),
                                new GrantEffectToTargetUntilEndOfTurnEffect(
                                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)))),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("Malakir Mire", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "MalakirMire";
    }
}
