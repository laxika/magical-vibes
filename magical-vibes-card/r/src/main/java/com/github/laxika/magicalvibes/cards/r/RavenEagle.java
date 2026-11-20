package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;

@CardRegistration(set = "TLA", collectorNumber = "116")
public class RavenEagle extends Card {

    public RavenEagle() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), exileAndInvestigate());
        addTrigger(Triggers.attacks(), exileAndInvestigate());
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                SequenceEffect.of(
                        new LoseLifeEffect(1, Subjects.eachOpponent()),
                        new GainLifeEffect(1)));
    }

    private static CardEffect exileAndInvestigate() {
        return SequenceEffect.of(
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard(),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.CREATURE)),
                        CreateTokenEffect.ofClueToken(1).withInvestigate(false)));
    }
}
