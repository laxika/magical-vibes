package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessEscapedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "221")
@CardRegistration(set = "THB", collectorNumber = "340")
@CardRegistration(set = "SLD", collectorNumber = "225")
@CardRegistration(set = "MUL", collectorNumber = "49")
@CardRegistration(set = "MUL", collectorNumber = "114")
@CardRegistration(set = "MUL", collectorNumber = "179")
public class KroxaTitanOfDeathsHunger extends Card {

    public KroxaTitanOfDeathsHunger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificeSelfUnlessEscapedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), discardThenLoseLife());
        addTrigger(Triggers.attacks(), discardThenLoseLife());

        addCastingOption(new GraveyardCast(null, "{B}{B}{R}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, false, true));
    }

    /**
     * Each opponent discards a card (chosen in APNAP order, discarded at once, CR 101.4), then each opponent who didn't
     * discard a nonland card this way loses 3 life.
     */
    private static CardEffect discardThenLoseLife() {
        return SequenceEffect.of(
                new DiscardEffect(1, Subjects.eachOpponent()),
                new LoseLifeEffect(3, Subjects.except(Subjects.eachOpponent(), Subjects.playersThisWay(
                        StepKind.DISCARD, new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
