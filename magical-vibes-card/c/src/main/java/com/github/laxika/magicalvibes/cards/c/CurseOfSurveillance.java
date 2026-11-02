package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDrawsCardsEqualToAttachedCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "46")
@CardRegistration(set = "DBL", collectorNumber = "46")
public class CurseOfSurveillance extends Card {

    public CurseOfSurveillance() {
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                new EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CURSE)));
    }
}
