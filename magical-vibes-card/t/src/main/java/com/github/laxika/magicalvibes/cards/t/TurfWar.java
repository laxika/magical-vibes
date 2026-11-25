package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "54")
@CardRegistration(set = "NCC", collectorNumber = "154")
public class TurfWar extends Card {

    private static final PermanentPredicate CONTESTED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.CONTESTED)));

    public TurfWar() {
        // "For each player, ... target land that player controls": one target per player, chosen as the trigger goes on
        // the stack; with a player who controls no land it can target, the ability is removed (CR 603.3d).
        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_PLAYER);
        target(TargetFilters.land(), 0, 99).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.CONTESTED));
        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanent(CONTESTED_LAND)),
                gainControlOfAContestedLandOf(Subjects.you()));
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(),
                TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerControlsPermanent(CONTESTED_LAND)),
                gainControlOfAContestedLandOf(Subjects.triggeringPlayer()));
    }

    /**
     * "That creature's controller gains control of one of those lands of their choice and untaps it", the lands those
     * the damaged player controls.
     */
    private static CardEffect gainControlOfAContestedLandOf(PlayerSubject damagedPlayer) {
        PlayerSubject creatureController = Subjects.controllerOf(Subjects.triggering());
        return SequenceEffect.of(
                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(damagedPlayer, CONTESTED_LAND),
                        "a contested land").chosenBy(creatureController),
                GainControlOfTargetEffect.of(Subjects.thisWay(StepKind.CHOOSE), Duration.PERMANENT)
                        .gainedBy(creatureController),
                new UntapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
