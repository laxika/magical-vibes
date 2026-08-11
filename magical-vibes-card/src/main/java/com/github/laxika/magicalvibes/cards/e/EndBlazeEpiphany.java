package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "134")
public class EndBlazeEpiphany extends Card {

    public EndBlazeEpiphany() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()))
                .addEffect(EffectSlot.SPELL, new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(new EventValue())));
    }
}
