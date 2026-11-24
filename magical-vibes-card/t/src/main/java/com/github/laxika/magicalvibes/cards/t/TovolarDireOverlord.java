package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BecomeNightEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "246")
@CardRegistration(set = "SLD", collectorNumber = "1612")
@CardRegistration(set = "DBL", collectorNumber = "246")
public class TovolarDireOverlord extends Card {

    public TovolarDireOverlord() {
        setBackFaceCard(new TovolarTheMidnightScourge());

        PermanentPredicate wolfOrWerewolf = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(wolfOrWerewolf),
                new DrawCardEffect(1));

        PermanentPredicate humanWerewolf = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(3, wolfOrWerewolf)),
                SequenceEffect.of(new BecomeNightEffect(),
                        ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(), humanWerewolf),
                                "Human Werewolves you control to transform"),
                        new TransformSelfEffect(Subjects.thisWay(StepKind.CHOOSE))));
    }

    @Override
    public String getBackFaceClassName() {
        return "TovolarTheMidnightScourge";
    }
}
