package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "268")
@CardRegistration(set = "C21", collectorNumber = "61")
public class FractalHarness extends Card {

    public FractalHarness() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateXTokenWithXCountersEffect(
                        "Fractal", 0, 0, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL),
                        CounterType.PLUS_ONE_PLUS_ONE),
                new AttachMatchingEquipmentToCreatedPermanentEffect(new PermanentIsSourcePermanentPredicate(), true));
        addTrigger(Triggers.attacks(), new DoublePlusOneCountersOnTargetCreatureEffect(Subjects.triggering()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
