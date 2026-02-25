package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerEquipmentOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "12")
public class KembaKhaRegent extends Card {

    public KembaKhaRegent() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenPerEquipmentOnSourceEffect(
                "Cat",
                2,
                2,
                CardColor.WHITE,
                List.of(CardSubtype.CAT),
                Set.of(),
                Set.of()
        ));
    }
}
