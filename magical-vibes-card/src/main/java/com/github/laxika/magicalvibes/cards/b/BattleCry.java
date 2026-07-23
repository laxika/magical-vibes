package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBlockerBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "5")
public class BattleCry extends Card {

    public BattleCry() {
        // Untap all white creatures you control.
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));

        // Whenever a creature blocks this turn, it gets +0/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new RegisterDelayedBlockerBoostEffect(0, 1));
    }
}
