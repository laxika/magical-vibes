package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "134")
@CardRegistration(set = "ECL", collectorNumber = "364")
public class EndBlazeEpiphany extends Card {

    public EndBlazeEpiphany() {
        target(TargetFilters.creature())
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ExileTopCardMayPlayThisTurnEffect(new EventValue(), false)
                                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).choosingOne()))
                .addSpellEffect(new DealDamageToTargetCreatureEffect(new XValue()));
    }
}
