package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDrawsCardsEqualToAttachedCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MID", collectorNumber = "46")
public class CurseOfSurveillance extends Card {

    public CurseOfSurveillance() {
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                new EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CURSE)));
    }
}
