package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetCreatureStatEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "17")
public class DazzlingReflection extends Card {

    public DazzlingReflection() {
        target(TargetFilters.creature())
                .addSpellEffect(new GainLifeEqualToTargetCreatureStatEffect(new TargetPower()))
                .addSpellEffect(new PreventNextDamageByTargetCreatureEffect(false));
    }
}
