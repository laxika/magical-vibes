package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "59")
public class MindSpiral extends Card {

    public MindSpiral() {
        addStaticEffect(new GiftEffect(1));
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(fishToken())));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new DrawCardEffect(3, Subjects.targetPlayer()));

        targetWhenGiftPromised(TargetFilters.creatureAnOpponentControls(), 0, 1, 1)
                .addSpellEffect(new ConditionalEffect(new GiftPromised(),
                        new TapPermanentsEffect(Subjects.target())))
                .addSpellEffect(new ConditionalEffect(new GiftPromised(),
                        new PutCountersEffect(CounterType.STUN)));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(1, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH),
                Set.of(), Set.of(), true);
    }
}
