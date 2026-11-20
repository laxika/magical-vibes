package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2501")
@CardRegistration(set = "VOC", collectorNumber = "2")
@CardRegistration(set = "VOC", collectorNumber = "40")
public class StrefanMaurerProgenitor extends Card {

    private static final CardAllOfPredicate VAMPIRE_CREATURE = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardSubtypePredicate(CardSubtype.VAMPIRE)));

    public StrefanMaurerProgenitor() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                CreateTokenEffect.ofBloodToken(new Sum(
                        new OpponentsWhoLostLifeThisTurn(),
                        new FixedIfCondition(new ControllerLostLifeThisTurn(1), 1, 0))));
        addTrigger(Triggers.attacks(),
                new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(2,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                                new PermanentIsTokenPredicate())),
                                        Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.atLeast(StepKind.SACRIFICE, 2),
                                        new PutCardToBattlefieldThenEffect(
                                                VAMPIRE_CREATURE,
                                                "Vampire creature",
                                                true,
                                                true,
                                                null,
                                                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target())))),
                        "Sacrifice two Blood tokens?"));
    }
}
