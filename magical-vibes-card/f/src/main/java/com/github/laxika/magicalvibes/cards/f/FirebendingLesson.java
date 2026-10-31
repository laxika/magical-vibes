package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "138")
public class FirebendingLesson extends Card {

    public FirebendingLesson() {
        addStaticEffect(new KickerEffect("{4}"));
        target(TargetFilters.creature())
                .addSpellEffect(new ConditionalReplacementEffect(new Kicked(),
                        new DealDamageToTargetCreatureEffect(2),
                        new DealDamageToTargetCreatureEffect(5)));
    }
}
