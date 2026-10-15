package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "58")
public class EnlightenedManiac extends Card {

    public EnlightenedManiac() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Eldrazi Horror", 3, 2, null,
                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR),
                Set.of(), Set.of()));
    }
}
