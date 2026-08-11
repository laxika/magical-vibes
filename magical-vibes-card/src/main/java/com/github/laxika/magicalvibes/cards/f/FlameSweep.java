package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "139")
public class FlameSweep extends Card {

    public FlameSweep() {
        // Flame Sweep deals 2 damage to each creature except for creatures you control with flying.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                2,
                false,
                false,
                new PermanentNotPredicate(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                )))
        ));
    }
}
