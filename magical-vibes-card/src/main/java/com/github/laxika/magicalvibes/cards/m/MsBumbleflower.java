package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "3")
@CardRegistration(set = "BLC", collectorNumber = "103")
public class MsBumbleflower extends Card {

    public MsBumbleflower() {
        PlayerPredicateTargetFilter opponentTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");

        SpellTarget playerTarget = target(opponentTarget);
        SpellTarget creatureTarget = target(TargetFilters.creature());

        DrawCardForTargetPlayerEffect opponentDraw = new DrawCardForTargetPlayerEffect(1, false, true);

        PutCounterOnTargetPermanentEffect counter =
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        GrantKeywordEffect flying = new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET);
        ConditionalEffect secondResolutionDraw = new ConditionalEffect(
                new NthAbilityResolutionThisTurn(2), new DrawCardEffect(2));

        registerEffectTargetIndex(opponentDraw, playerTarget.getIndex());
        registerEffectTargetIndex(counter, creatureTarget.getIndex());
        registerEffectTargetIndex(flying, creatureTarget.getIndex());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(opponentDraw, counter, flying, secondResolutionDraw)));
    }
}
