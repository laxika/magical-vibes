package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "190")
public class KulrathKnight extends Card {

    public KulrathKnight() {
        // Flying and Wither are keywords (auto-loaded from Scryfall).
        // Creatures your opponents control with counters on them can't attack or block.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentHasCountersPredicate(CounterType.ANY))),
                "Creatures your opponents control with counters on them can't attack or block"));
    }
}
