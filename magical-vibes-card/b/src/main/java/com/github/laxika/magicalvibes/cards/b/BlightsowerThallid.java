package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

public class BlightsowerThallid extends Card {

    public BlightsowerThallid() {
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, phyrexianSaproling());
        addTrigger(Triggers.dies(ObjectRelation.THIS), phyrexianSaproling());
    }

    private static CreateTokenEffect phyrexianSaproling() {
        return new CreateTokenEffect(
                "Phyrexian Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        );
    }
}
