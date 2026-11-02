package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "24")
public class PerennialGravewarden extends Card {

    public PerennialGravewarden() {
        // When this creature enters, it perpetually gets +1/+1.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new PerpetuallyBoostSourceEffect(1, 1));

        // At the beginning of your end step, if a creature entered and a creature died this turn,
        // return this card from your graveyard to the battlefield tapped.
        addTrigger(new TriggerCondition(Triggers.endStep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new PermanentEnteredThisTurn(
                                        new CardTypePredicate(CardType.CREATURE), 1, CountScope.ANY_PLAYER),
                                new CreaturesDiedThisTurnAtLeast(1))),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)));
    }
}
