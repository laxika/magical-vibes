package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "172")
@CardRegistration(set = "DDN", collectorNumber = "28")
@CardRegistration(set = "TSR", collectorNumber = "178")
public class OrcishCannonade extends Card {

    public OrcishCannonade() {
        addSimultaneousSpellEffects(DealDamageEffect.toAnyTarget(2),
                DealDamageEffect.to(Subjects.you(), 3));
        addSpellEffect(new DrawCardEffect(1));
    }
}
