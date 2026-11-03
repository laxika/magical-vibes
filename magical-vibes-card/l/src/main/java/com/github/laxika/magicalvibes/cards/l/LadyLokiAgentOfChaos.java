package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "54")
@CardRegistration(set = "MSC", collectorNumber = "365")
public class LadyLokiAgentOfChaos extends Card {

    public LadyLokiAgentOfChaos() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY),
                                new CardSubtypePredicate(CardSubtype.VILLAIN))))
                        .nthThisTurn(1),
                new ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffect());
    }
}
