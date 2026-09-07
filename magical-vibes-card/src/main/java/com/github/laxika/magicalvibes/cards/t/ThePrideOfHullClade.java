package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.amount.TotalToughnessOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "172")
@CardRegistration(set = "MKM", collectorNumber = "352")
@CardRegistration(set = "MKM", collectorNumber = "382")
public class ThePrideOfHullClade extends Card {

    public ThePrideOfHullClade() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Max(new Fixed(0), new TotalToughnessOfControlledCreatures())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(
                        new BoostTargetCreatureEffect(1, 0),
                        new CanAttackAsThoughNoDefenderEffect(true),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new DrawCardEffect(new SourceToughness()))),
                "{2}{U}{U}: Target creature you control gets +1/+0, gains \"Whenever this creature deals combat damage to a player, draw cards equal to its toughness,\" and can attack as though it didn't have defender until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
