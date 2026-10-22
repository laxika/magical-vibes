package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MB2", collectorNumber = "334")
@CardRegistration(set = "MB2", collectorNumber = "571")
public class ZoneOfFlame extends Card {

    public ZoneOfFlame() {
        setEnchantZone(true);

        // Tokens aren't cards, so token entries and departures don't count.
        DealDamageToPlayersEffect trigger =
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardNotPredicate(new CardIsTokenPredicate()), trigger),
                TriggerMode.ONCE_PER_BATCH);
        addEffect(EffectSlot.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()), trigger, true));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ConditionalEffect(new NotCondition(new SourceIsToken()), trigger));
    }
}
