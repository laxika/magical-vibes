package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "134")
@CardRegistration(set = "ECL", collectorNumber = "364")
public class EndBlazeEpiphany extends Card {

    public EndBlazeEpiphany() {
        target(TargetFilters.creature())
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(new EventValue())))
                .addSpellEffect(new DealDamageToTargetCreatureEffect(new XValue()));
    }
}
