package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnlyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "SLD", collectorNumber = "1625")
public class KorvoldFaeCursedKing extends Card {

    private static final SacrificePermanentThenEffect SACRIFICE_ANOTHER_PERMANENT =
            new SacrificePermanentThenEffect(
                    new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                    null,
                    "another permanent");

    private static final SequenceEffect SACRIFICE_TRIGGER = SequenceEffect.of(
            new PutCountersOnSourceEffect(1, 1, 1),
            new DrawCardEffect());

    public KorvoldFaeCursedKing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SACRIFICE_ANOTHER_PERMANENT);
        addEffect(EffectSlot.ON_ATTACK, SACRIFICE_ANOTHER_PERMANENT);

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, SACRIFICE_TRIGGER);
        addEffect(EffectSlot.ON_DEATH, new SacrificeOnlyEffect(SACRIFICE_TRIGGER));
    }
}
