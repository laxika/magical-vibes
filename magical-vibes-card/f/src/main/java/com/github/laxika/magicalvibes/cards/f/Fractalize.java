package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "51")
public class Fractalize extends Card {

    public Fractalize() {
        // Until end of turn, target creature becomes a green and blue Fractal with base power
        // and toughness each equal to X plus 1.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.GREEN))
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.BLUE, true))
                .addEffect(EffectSlot.SPELL, new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FRACTAL))
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessToAmountEffect(
                        new Sum(new Fixed(1), new XValue()), new Sum(new Fixed(1), new XValue()),
                        GrantScope.TARGET));
    }
}
