package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "14")
public class GoblinMoraleSergeant extends Card {

    public GoblinMoraleSergeant() {
        addTrigger(Triggers.enlists().withFilter(new PermanentNotPredicate(new PermanentIsTokenPredicate())),
                new MayEffect(SequenceEffect.of(
                        ConjureCardsEffect.duplicateOf(Subjects.triggering(),
                                ConjureDestination.intoTopCardsOfLibraryAtRandom(5)),
                        PerpetuallyBoostCardEffect.toCards(Subjects.cardsThisWay(StepKind.CONJURE), 1, 0),
                        PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(
                                Subjects.cardsThisWay(StepKind.CONJURE), Keyword.HASTE)),
                        "Conjure a duplicate of the enlisted creature into the top five cards of your library?"));
    }
}
