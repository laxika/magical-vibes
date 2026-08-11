package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "51")
public class LethargyTrap extends Card {

    public LethargyTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{U}")),
                new AnyPlayerControlsPermanentCount(3, new PermanentIsAttackingPredicate()),
                false));
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-3, 0, new PermanentIsAttackingPredicate()));
    }
}
