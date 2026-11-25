package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1096")
@CardRegistration(set = "SLD", collectorNumber = "1867")
@CardRegistration(set = "AA1", collectorNumber = "15")
@CardRegistration(set = "MH1", collectorNumber = "229")
public class SwordOfTruthAndJustice extends Card {

    public SwordOfTruthAndJustice() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE, CardColor.BLUE), Subjects.attachedCreature()));
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, Subjects.creaturesYouControl(), "a creature you control"),
                        new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)),
                        new ProliferateEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
