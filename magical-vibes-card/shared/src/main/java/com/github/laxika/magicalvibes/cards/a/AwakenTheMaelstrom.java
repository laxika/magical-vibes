package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenPermanentYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

public class AwakenTheMaelstrom extends Card {

    public AwakenTheMaelstrom() {
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");
        var opponentPermanent = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a permanent an opponent controls.");

        target(anyPlayer).addSpellEffect(new DrawCardForTargetPlayerEffect(2, false, true));
        addSpellEffect(new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.ARTIFACT), "artifact"),
                "Put an artifact card from your hand onto the battlefield?"));
        addSpellEffect(new CreateTokenCopyOfChosenPermanentYouControlEffect());
        addSpellEffect(new DistributeCountersAmongControlledCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
        target(opponentPermanent).addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
