package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "180")
@CardRegistration(set = "CMD", collectorNumber = "137")
@CardRegistration(set = "CMM", collectorNumber = "262")
@CardRegistration(set = "CMA", collectorNumber = "88")
public class SulfurousBlast extends Card {

    public SulfurousBlast() {
        addSpellEffect(new ConditionalEffect(
                new CastDuringMainPhase(), DealDamageEffect.toEachCreature(3).andTo(Subjects.eachPlayer())));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(new CastDuringMainPhase()), DealDamageEffect.toEachCreature(2)
                        .andTo(Subjects.eachPlayer())));
    }
}
