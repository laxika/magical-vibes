package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "158")
public class SourbreadAuntie extends Card {

    public SourbreadAuntie() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new BlightEffect(2, new CreateTokenEffect(
                        2, "Goblin", 1, 1, CardColor.BLACK,
                        Set.of(CardColor.BLACK, CardColor.RED), List.of(CardSubtype.GOBLIN))),
                "Blight 2?"));
    }
}
