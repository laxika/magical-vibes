package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsThenEffect;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2501")
public class StrefanMaurerProgenitor extends Card {

    private static final CardAllOfPredicate VAMPIRE_CREATURE = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardSubtypePredicate(CardSubtype.VAMPIRE)));

    public StrefanMaurerProgenitor() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                CreateTokenEffect.ofBloodToken(new Sum(
                        new OpponentsWhoLostLifeThisTurn(),
                        new FixedIfCondition(new ControllerLostLifeThisTurn(1), 1, 0))));
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(
                        new SacrificePermanentsThenEffect(
                                2,
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                new PutCardToBattlefieldThenEffect(
                                        VAMPIRE_CREATURE,
                                        "Vampire creature",
                                        true,
                                        true,
                                        null,
                                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)),
                                "two Blood tokens"),
                        "Sacrifice two Blood tokens?"));
    }
}
