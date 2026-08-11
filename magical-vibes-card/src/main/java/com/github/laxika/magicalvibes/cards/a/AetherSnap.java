package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "DST", collectorNumber = "37")
public class AetherSnap extends Card {

    public AetherSnap() {
        addEffect(EffectSlot.SPELL, new RemoveAllCountersFromAllPermanentsEffect());
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentIsTokenPredicate()));
    }
}
