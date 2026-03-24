package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "30")
public class RallyingRoar extends Card {

    public RallyingRoar() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.SPELL, new UntapAllControlledPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
