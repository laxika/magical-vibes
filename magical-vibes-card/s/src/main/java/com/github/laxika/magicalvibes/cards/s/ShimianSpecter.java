package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "109")
@CardRegistration(set = "FUT", collectorNumber = "76")
public class ShimianSpecter extends Card {

    public ShimianSpecter() {
        // The damaged player is baked in as the trigger's target, so no target(...) is declared.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ChooseCardsFromTargetHandEffect(
                                1, List.of(CardType.LAND), HandChoiceDestination.KEEP_IN_HAND),
                        ExileCardsWithSameNameEffect.all(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .searching(Subjects.targetPlayerDeclaredElsewhere(),
                                        Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY)));
    }
}
