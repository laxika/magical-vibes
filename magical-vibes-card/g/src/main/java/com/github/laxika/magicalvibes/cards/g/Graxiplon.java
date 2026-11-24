package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerMatches;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsCreaturesSharingCreatureTypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "86")
public class Graxiplon extends Card {

    public Graxiplon() {
        addStaticEffect(CantBeBlockedIfDefenderControlsMatchingPermanentEffect.whileDefendingPlayer(
                new NotCondition(new TargetPlayerMatches(new PlayerControlsCreaturesSharingCreatureTypePredicate(3)))));
    }
}
