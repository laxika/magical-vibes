package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "105")
public class TunnelIgnus extends Card {

    public TunnelIgnus() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentEnteredThisTurn(
                        new CardTypePredicate(CardType.LAND), 2, CountScope.TARGET_PLAYER)),
                new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER));
    }
}
