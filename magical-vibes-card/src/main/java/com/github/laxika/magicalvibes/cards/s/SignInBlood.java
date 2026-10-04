package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M10", collectorNumber = "112")
@CardRegistration(set = "M11", collectorNumber = "117")
@CardRegistration(set = "M13", collectorNumber = "110")
@CardRegistration(set = "M15", collectorNumber = "114")
@CardRegistration(set = "DDD", collectorNumber = "49")
@CardRegistration(set = "MM2", collectorNumber = "97")
@CardRegistration(set = "GVL", collectorNumber = "49")
@CardRegistration(set = "STA", collectorNumber = "32")
@CardRegistration(set = "GN3", collectorNumber = "61")
@CardRegistration(set = "CMD", collectorNumber = "101")
@CardRegistration(set = "C14", collectorNumber = "161")
@CardRegistration(set = "DSC", collectorNumber = "156")
@CardRegistration(set = "SCD", collectorNumber = "107")
@CardRegistration(set = "CM2", collectorNumber = "77")
@CardRegistration(set = "ARC", collectorNumber = "25")
public class SignInBlood extends Card {

    public SignInBlood() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(2))
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
