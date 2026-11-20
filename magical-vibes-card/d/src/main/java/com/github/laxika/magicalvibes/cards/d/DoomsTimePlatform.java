package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersAndGainSuspendEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "103")
@CardRegistration(set = "MSC", collectorNumber = "431")
public class DoomsTimePlatform extends Card {

    public DoomsTimePlatform() {
        addTrigger(Triggers.youAttack(),
                ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                PutTimeCountersAndGainSuspendEffect.onCardsExiledThisWay(2));
    }
}
