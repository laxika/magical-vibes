package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "206")
public class Subdue extends Card {

    public Subdue() {
        target(TargetFilters.creature())
                .addSpellEffect(PreventDamageEffect.allCombatByTargetCreatures())
                .addSpellEffect(new BoostTargetCreatureEffect(new Fixed(0), new TargetManaValue()));
    }
}
