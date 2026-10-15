package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;

@CardRegistration(set = "MSC", collectorNumber = "73")
@CardRegistration(set = "MSC", collectorNumber = "390")
public class AntManElusiveAvenger extends Card {

    public AntManElusiveAvenger() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate())));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
