package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;

@CardRegistration(set = "MID", collectorNumber = "85")
@CardRegistration(set = "DBL", collectorNumber = "85")
public class BanebladeScoundrel extends Card {

    public BanebladeScoundrel() {
        setBackFaceCard(new BaneclawMarauder());

        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostAllCreaturesEffect(-1, -1, new PermanentBlockingSourcePredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "BaneclawMarauder";
    }
}
