package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "182")
@CardRegistration(set = "SOS", collectorNumber = "348")
public class ConciliatorsDuelist extends Card {

    public ConciliatorsDuelist() {
        // When this creature enters, draw a card. Each player loses 1 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));

        // Repartee — Whenever you cast an instant or sorcery spell that targets a creature,
        // exile up to one target creature. Return that card to the battlefield under its
        // owner's control at the beginning of the next end step.
        target(TargetFilters.creature(), 0, 1).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())),
                FlickerEffect.exileTargetReturnAtEndStep());
    }
}
