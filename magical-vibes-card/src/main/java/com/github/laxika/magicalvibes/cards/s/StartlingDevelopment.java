package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "68")
public class StartlingDevelopment extends Card {

    public StartlingDevelopment() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
                .addEffect(EffectSlot.SPELL, new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.SERPENT))
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(4, 4));
        addCycling("{1}");
    }
}
