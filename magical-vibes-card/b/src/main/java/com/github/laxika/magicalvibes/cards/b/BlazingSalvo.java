package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "178")
@CardRegistration(set = "DDK", collectorNumber = "58")
public class BlazingSalvo extends Card {

    public BlazingSalvo() {
        target(TargetFilters.creature()).addSpellEffect(UnlessPaysEffect.unlessPays(
                Subjects.controllerOf(Subjects.target()), DamageFromSourceCost.of(5),
                DealDamageEffect.toTargetCreature(3)));
    }
}
