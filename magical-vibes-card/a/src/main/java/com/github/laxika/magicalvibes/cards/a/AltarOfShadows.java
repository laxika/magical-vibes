package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "143")
public class AltarOfShadows extends Card {

    public AltarOfShadows() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new AwardManaEffect(ManaColor.BLACK, new CountersOnSource(CounterType.CHARGE)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new DestroyTargetPermanentEffect(), new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{7}, {T}: Destroy target creature. Then put a charge counter on Altar of Shadows.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }
}
