package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "114")
public class Starfall extends Card {

    public Starfall() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(3))
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsEnchantmentPredicate()),
                        DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 3)));
    }
}
