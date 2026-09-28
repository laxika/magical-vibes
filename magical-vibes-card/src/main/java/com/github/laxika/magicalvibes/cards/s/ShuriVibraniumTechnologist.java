package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "746")
public class ShuriVibraniumTechnologist extends Card {

    public ShuriVibraniumTechnologist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 colorless Robot Hero artifact creature token with flying.",
                        new CreateTokenEffect("Robot", 1, 1, null,
                                List.of(CardSubtype.ROBOT, CardSubtype.HERO),
                                Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption("Draw a card.", new DrawCardEffect(1)
        )))));
    }
}
