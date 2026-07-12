package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "110")
public class DevotedDruid extends Card {

    public DevotedDruid() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PutCounterOnSourceCost(), new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Put a -1/-1 counter on this creature: Untap this creature."));
    }
}
