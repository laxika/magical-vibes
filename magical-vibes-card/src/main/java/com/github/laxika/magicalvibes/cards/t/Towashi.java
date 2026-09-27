package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "67")
public class Towashi extends Card {

    public Towashi() {
        var modifiedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsModifiedPredicate()));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES, modifiedCreature));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                new DrawCardEffect(1), GrantScope.ALL_OWN_CREATURES, modifiedCreature));

        target(TargetFilters.creatureYouControl(), 1, 3)
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                                CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));
    }
}
