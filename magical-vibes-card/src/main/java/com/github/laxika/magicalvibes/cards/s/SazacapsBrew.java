package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "151")
public class SazacapsBrew extends Card {

    public SazacapsBrew() {
        addEffect(EffectSlot.STATIC, new GiftEffect(1));
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(fishToken())));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(2, false, true));

        targetWhenGiftPromised(TargetFilters.creatureYouControl(), 0, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                        new BoostTargetCreatureEffect(2, 0)));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(1, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH),
                Set.of(), Set.of(), true);
    }
}
