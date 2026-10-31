package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "30")
public class Restrain extends Card {

    public Restrain() {
        target(TargetFilters.attackingCreature())
                .addSpellEffect(PreventDamageEffect.allCombatByTargetCreatures())
                .addSpellEffect(new DrawCardEffect());
    }
}
