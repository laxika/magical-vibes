package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsByAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "49")
public class ElugeTheShorelessSea extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    private static final PermanentAllOfPredicate FLOODED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.FLOOD)));

    public ElugeTheShorelessSea() {
        DynamicAmount islands = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER);
        DynamicAmount floodedLands = new PermanentCount(FLOODED_LAND, CountScope.CONTROLLER);

        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(islands, islands));

        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.FLOOD))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype.ISLAND, CounterType.FLOOD));
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.FLOOD))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype.ISLAND, CounterType.FLOOD));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControllerCastSpellThisTurn(INSTANT_OR_SORCERY)),
                new ReduceColoredCastCostForMatchingSpellsByAmountEffect(
                        INSTANT_OR_SORCERY, ManaColor.BLUE, floodedLands,
                        CostModificationScope.SELF)));
    }
}
