package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "182")
public class LongstalkBrawl extends Card {

    public LongstalkBrawl() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                TargetOpponentCreatesTokenEffect.gift(fishToken())));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature you control"
        )).addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new GiftPromised(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Second target must be a creature you don't control"
        )).addEffect(EffectSlot.SPELL, new FightTargetsEffect());
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(1, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH),
                Set.of(), Set.of(), true);
    }
}
