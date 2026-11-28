package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "16")
public class MineSecurity extends Card {

    public MineSecurity() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                // Flametongue Kavu, which perpetually gains "You may pay {0} rather than pay this spell's mana cost."
                ConjureCardsEffect.named("PLS", "60", ConjureDestination.intoTopCardsOfLibraryAtRandom(8))
                        .withAbilities(List.of(StaticAbility.of(List.of(
                                AlternativeCostForSpellsEffect.perpetualZeroCostForThisSpell())))));
    }
}
