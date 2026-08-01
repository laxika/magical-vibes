package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "RTR", collectorNumber = "139")
public class WildBeastmaster extends Card {

    public WildBeastmaster() {
        // Whenever this creature attacks, each other creature you control gets
        // +X/+X until end of turn, where X is this creature's power.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                new SourcePower(),
                new SourcePower(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
    }
}
