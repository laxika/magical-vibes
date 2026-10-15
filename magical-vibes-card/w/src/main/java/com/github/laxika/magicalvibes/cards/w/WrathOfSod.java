package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfSourceCardColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "103")
public class WrathOfSod extends Card {

    public WrathOfSod() {
        addEffect(EffectSlot.SPELL, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.MANABOND, 1, new PermanentIsCreaturePredicate(),
                EachPermanentScope.ALL_PLAYERS));
        addEffect(EffectSlot.SPELL, new GrantEffectsToCounterBearersEffect(CounterType.MANABOND, List.of(
                new SetCardTypesEffect(Set.of(CardType.LAND), GrantScope.ALL_LANDS),
                new LoseAllCreatureTypesEffect(GrantScope.ALL_LANDS),
                new LoseAllLandTypesEffect(GrantScope.ALL_LANDS, null),
                new LosesAllAbilitiesEffect(GrantScope.ALL_LANDS),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true, null, List.of(new AwardManaOfSourceCardColorsEffect()),
                                "{T}: Add one mana of this card's color."),
                        GrantScope.ALL_LANDS))));
    }
}
