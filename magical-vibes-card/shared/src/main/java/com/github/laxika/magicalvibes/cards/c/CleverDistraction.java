package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class CleverDistraction extends Card {

    private static final PermanentAllOfPredicate DEFENDING_PLAYER_CREATURE =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledByDefendingPlayerPredicate()));

    public CleverDistraction() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(),
                        new TapPermanentsEffect(TapUntapScope.TARGET, DEFENDING_PLAYER_CREATURE));
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
