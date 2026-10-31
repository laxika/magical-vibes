package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MID", collectorNumber = "241")
@CardRegistration(set = "OTP", collectorNumber = "54")
@CardRegistration(set = "DBL", collectorNumber = "241")
@CardRegistration(set = "OTC", collectorNumber = "242")
public class SiphonInsight extends Card {

    public SiphonInsight() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new LookAtTopCardsOfTargetLibraryEffect(2,
                        TargetLibraryAction.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM));
        addCastingOption(new FlashbackCast("{1}{U}{B}"));
    }
}
