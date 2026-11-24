package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "232")
public class TaskmasterMercenaryMimic extends Card {

    public TaskmasterMercenaryMimic() {
        target(0, 1)
                .addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                        BecomeCopyOfSubjectEffect.ofCreatureOrCard(Subjects.targetCard(
                                        new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS))
                                .lasting(Duration.UNTIL_YOUR_NEXT_TURN)
                                .except(CopyExceptions.named("Taskmaster, Mercenary Mimic").alsoLegendary()
                                        .alsoACreatureOfTypes(CardSubtype.HUMAN, CardSubtype.MERCENARY,
                                                CardSubtype.VILLAIN)));
    }
}
