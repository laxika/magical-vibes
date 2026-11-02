package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "286")
@CardRegistration(set = "MB2", collectorNumber = "522")
public class PlainWalker extends Card {

    public PlainWalker() {
        addStaticEffect(new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentIsPlaneswalkerPredicate()));
        addTrigger(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
                new PlaneswalkEffect());
    }
}
