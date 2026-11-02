package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "11")
public class SanguineSoothsayer extends Card {

    public SanguineSoothsayer() {
        addEffect(EffectSlot.ON_ATTACK, new ConjureCardIntoControllerLibraryEffect(
                "WOT", "35", List.of(
                        StaticAbility.of(List.of(
                                AlternativeCostForSpellsEffect.perpetualZeroCostForThisSpell())),
                        new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1)))));
    }
}
