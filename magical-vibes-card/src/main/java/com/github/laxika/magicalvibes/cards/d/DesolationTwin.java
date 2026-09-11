package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "6")
public class DesolationTwin extends Card {

    public DesolationTwin() {
        addEffect(EffectSlot.ON_SELF_CAST, new CreateTokenEffect(
                "Eldrazi", 10, 10, null, List.of(CardSubtype.ELDRAZI), Set.of(), Set.of()));
    }
}
