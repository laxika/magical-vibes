package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "96")
public class TukatongueThallid extends Card {

    public TukatongueThallid() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        ));
    }
}
