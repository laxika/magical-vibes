package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "185")
public class ArchOfOrazca extends Card {

    public ArchOfOrazca() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new DrawCardEffect(1)),
                "{5}, {T}: Draw a card. Activate only if you have the city's blessing."
        ).withActivationCondition(new ControllerHasCityBlessing(),
                "Activate only if you have the city's blessing"));
    }
}
