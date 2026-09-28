package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "127")
public class BrainstealerDragon extends Card {

    public BrainstealerDragon() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect());
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()))),
                        new LoseLifeToOwnerOfTriggeringPermanentEffect()));
    }
}
