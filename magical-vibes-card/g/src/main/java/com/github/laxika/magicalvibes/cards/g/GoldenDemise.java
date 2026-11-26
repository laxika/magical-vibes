package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "RIX", collectorNumber = "73")
public class GoldenDemise extends Card {

    public GoldenDemise() {
        addSpellEffect(new AscendEffect());
        addSpellEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                BoostEffect.allCreatures(-2, -2,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
        addSpellEffect(new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                BoostEffect.allCreatures(-2, -2)));
    }
}
