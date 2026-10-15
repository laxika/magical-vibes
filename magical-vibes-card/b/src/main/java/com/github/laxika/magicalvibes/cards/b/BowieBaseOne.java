package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayerDirection;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerDirectionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "571")
public class BowieBaseOne extends Card {

    public BowieBaseOne() {
        PermanentPredicate creatureControlledByPlayerToLeft = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByPlayerDirectionPredicate(PlayerDirection.LEFT)));

        target(new PermanentPredicateTargetFilter(
                creatureControlledByPlayerToLeft,
                "Target must be a creature controlled by the player to your left"))
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new GoadTargetCreatureUntilNextTurnEffect(creatureControlledByPlayerToLeft));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.TARGET));
    }
}
