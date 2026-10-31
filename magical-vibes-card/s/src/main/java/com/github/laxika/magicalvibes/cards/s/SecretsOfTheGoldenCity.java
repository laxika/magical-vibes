package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "RIX", collectorNumber = "52")
public class SecretsOfTheGoldenCity extends Card {

    public SecretsOfTheGoldenCity() {
        addSpellEffect(new AscendEffect());
        addSpellEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                new DrawCardEffect(3)));
        addSpellEffect(new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                new DrawCardEffect(2)));
    }
}
