package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "104")
public class GravpackMonoist extends Card {

    public GravpackMonoist() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Robot", 2, 2, null,
                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT), true));
    }
}
