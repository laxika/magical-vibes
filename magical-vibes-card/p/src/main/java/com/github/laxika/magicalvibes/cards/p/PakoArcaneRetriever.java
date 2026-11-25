package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "13")
public class PakoArcaneRetriever extends Card {

    private static final String PARTNER_NAME = "Haldan, Avid Arcanist";

    public PakoArcaneRetriever() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Haldan, Avid Arcanist into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addTrigger(Triggers.attacks(),
                ExileTopCardOfOwnLibraryEffect.linkedToSource(Subjects.eachPlayer()),
                PutCounterOnExiledCardsEffect.onCardsExiledThisWay(CounterType.FETCH),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, AmountThisWay.exiledCards(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))));
    }
}
