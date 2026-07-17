package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ALA", collectorNumber = "187")
public class RealmRazer extends Card {

    public RealmRazer() {
        // Exile all lands on enter; when Realm Razer leaves, return them tapped under their owners' control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAllPermanentsUntilSourceLeavesEffect(new PermanentIsLandPredicate(), true));
    }
}
