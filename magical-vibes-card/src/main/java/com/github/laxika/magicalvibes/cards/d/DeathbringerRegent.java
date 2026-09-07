package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "96")
public class DeathbringerRegent extends Card {

    public DeathbringerRegent() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AllOf(List.of(
                        new CastFromZone(Zone.HAND),
                        new AnyPlayerControlsPermanentCount(5, creature, true)
                )),
                new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        creature,
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                )))));
    }
}
