package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "27")
public class SaviorOfTheSmall extends Card {

    public SaviorOfTheSmall() {
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3))))
                        .targetGraveyard(true)
                        .build());
    }
}
