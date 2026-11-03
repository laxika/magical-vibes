package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "2")
public class ImprovisingAerialist extends Card {

    public ImprovisingAerialist() {
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                new PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect(
                        Keyword.FLYING,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardKeywordPredicate(Keyword.FLYING))))));
    }
}
