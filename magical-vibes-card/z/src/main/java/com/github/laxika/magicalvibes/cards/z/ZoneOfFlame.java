package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "334")
@CardRegistration(set = "MB2", collectorNumber = "571")
public class ZoneOfFlame extends Card {

    public ZoneOfFlame() {
        setEnchantZone(true);

        // Tokens aren't cards, so token entries and departures don't count.
        DealDamageToPlayersEffect trigger =
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
        addTrigger(new TriggerCondition(Triggers.enters(null, ObjectRelation.ANY)), TriggerOptions.ONE_OR_MORE,
                new TriggeringCardConditionalEffect(new CardNotPredicate(new CardIsTokenPredicate()), trigger));
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.NONTOKEN, ObjectRelation.ANOTHER)),
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.NONTOKEN, ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(trigger))));
    }
}
