package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect;

@CardRegistration(set = "40K", collectorNumber = "139")
public class TheRuinousPowers extends Card {

    public TheRuinousPowers() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect());
    }
}
