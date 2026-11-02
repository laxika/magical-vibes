package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C17", collectorNumber = "31")
public class HungryLynx extends Card {

    public HungryLynx() {
        PermanentHasSubtypePredicate cats = new PermanentHasSubtypePredicate(CardSubtype.CAT);

        addStaticEffect(new GrantEffectEffect(
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.RAT)),
                GrantScope.ALL_OWN_CREATURES,
                cats));

        setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Rat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.RAT), Set.of(Keyword.DEATHTOUCH), Set.of()),
                PlayerRelation.OPPONENT));

        addTrigger(Triggers.creatureDies(new PermanentHasSubtypePredicate(CardSubtype.RAT), ObjectRelation.ANY),
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.CAT),
                                new PermanentControlledBySourceControllerPredicate())),
                        EachPermanentScope.ALL_PLAYERS));
    }
}
