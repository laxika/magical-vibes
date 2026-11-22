package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "114")
@CardRegistration(set = "GK2", collectorNumber = "13")
public class IsperiaTheInscrutable extends Card {

    public IsperiaTheInscrutable() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ChooseCardNameAtResolutionEffect(),
                RevealCardsFromHandEffect.hand(Subjects.triggeringPlayer()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, named),
                        new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardKeywordPredicate(Keyword.FLYING))))));
    }
}
