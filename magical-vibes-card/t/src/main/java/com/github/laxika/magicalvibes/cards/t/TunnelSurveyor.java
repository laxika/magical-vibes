package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "76")
public class TunnelSurveyor extends Card {

    public TunnelSurveyor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                "Glimmer", 1, 1, CardColor.WHITE, List.of(CardSubtype.GLIMMER),
                Set.of(), Set.of(CardType.ENCHANTMENT)));
    }
}
