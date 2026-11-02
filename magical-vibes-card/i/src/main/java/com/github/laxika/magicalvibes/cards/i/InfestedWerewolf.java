package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

public class InfestedWerewolf extends Card {

    private static final CreateTokenEffect CREATE_INSECTS = new CreateTokenEffect(
            2, "Insect", 1, 1, CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(), Set.of());

    public InfestedWerewolf() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CREATE_INSECTS);
        addTrigger(Triggers.attacks(), CREATE_INSECTS);
    }
}
