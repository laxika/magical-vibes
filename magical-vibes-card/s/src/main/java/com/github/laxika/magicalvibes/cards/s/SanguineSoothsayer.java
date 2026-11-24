package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "11")
public class SanguineSoothsayer extends Card {

    public SanguineSoothsayer() {
        addTrigger(Triggers.attacks(), ConjureCardsEffect.named(
                        "WOT", "35", ConjureDestination.intoTopCardsOfLibraryAtRandom(15))
                .withAbilities(List.of(
                        StaticAbility.of(List.of(
                                AlternativeCostForSpellsEffect.perpetualZeroCostForThisSpell())),
                        new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1)))));
    }
}
