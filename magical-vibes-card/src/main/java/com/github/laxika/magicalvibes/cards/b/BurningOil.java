package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "84")
public class BurningOil extends Card {

    public BurningOil() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsBlockingPredicate()
                )),
                "Target must be an attacking or blocking creature"
        )).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addCastingOption(new FlashbackCast("{3}{W}"));
    }
}
