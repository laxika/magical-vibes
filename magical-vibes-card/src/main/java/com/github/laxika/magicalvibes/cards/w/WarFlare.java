package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FRF", collectorNumber = "158")
public class WarFlare extends Card {

    public WarFlare() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 1));
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED,
                new PermanentIsCreaturePredicate()));
    }
}
