package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SCG", collectorNumber = "107")
public class TorrentOfFire extends Card {

    public TorrentOfFire() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate())));
    }
}
