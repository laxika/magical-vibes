package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "206")
public class Subdue extends Card {

    public Subdue() {
        target(TargetFilters.creature())
                .addSpellEffect(PreventDamageEffect.allCombatByTargetCreatures())
                .addSpellEffect(BoostEffect.target(new Fixed(0), new TargetManaValue()));
    }
}
