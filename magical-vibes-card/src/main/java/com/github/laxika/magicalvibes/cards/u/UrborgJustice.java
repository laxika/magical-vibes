package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

/**
 * Target opponent sacrifices a creature of their choice for each creature put into your graveyard
 * from the battlefield this turn.
 *
 * <p>The creature filter is wrapped in {@link PermanentAllOfPredicate} so the sacrifice routes
 * through the multi-permanent choice instead of the single-select "sacrifice a creature" primitive,
 * which ignores the count (same trick as Malfegor).
 */
@CardRegistration(set = "WTH", collectorNumber = "84")
public class UrborgJustice extends Card {

    public UrborgJustice() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new CreatureDeathsThisTurn(CountScope.CONTROLLER),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.TARGET_PLAYER));
    }
}
