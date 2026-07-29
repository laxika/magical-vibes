package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "212")
public class Decomposition extends Card {

    public Decomposition() {
        // Enchant black creature
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)))),
                "Target must be a black creature"
        ))
                // Enchanted creature has "Cumulative upkeep-Pay 1 life."
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        CumulativeUpkeepEffect.life(1), GrantScope.ENCHANTED_CREATURE))

                // When enchanted creature dies, its controller loses 2 life.
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new EnchantedCreatureControllerLosesLifeEffect(2));
    }
}
