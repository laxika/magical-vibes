package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "31")
@CardRegistration(set = "DSC", collectorNumber = "59")
public class DemolisherSpawn extends Card {

    public DemolisherSpawn() {
        // Delirium — Whenever this creature attacks, other attacking creatures get +4/+4 until end
        // of turn.
        var otherAttackers = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new Delirium(), new BoostAllCreaturesEffect(4, 4, otherAttackers)));
    }
}
