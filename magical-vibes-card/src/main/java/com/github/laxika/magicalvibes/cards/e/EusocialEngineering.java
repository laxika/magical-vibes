package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "181")
public class EusocialEngineering extends Card {

    public EusocialEngineering() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Robot", 2, 2, null,
                        List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT)));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{G}"))));
    }
}
