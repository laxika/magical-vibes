package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "103")
public class WorldgorgerDragon extends Card {

    public WorldgorgerDragon() {
        PermanentPredicate otherPermanentsYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAllPermanentsUntilSourceLeavesEffect(otherPermanentsYouControl, false));
    }
}
