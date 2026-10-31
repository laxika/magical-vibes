package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "240")
public class ElephantAmbush extends Card {

    public ElephantAmbush() {
        addSpellEffect(new CreateTokenEffect(
                "Elephant",
                3,
                3,
                CardColor.GREEN,
                List.of(CardSubtype.ELEPHANT),
                Set.of(),
                Set.of()));
        addCastingOption(new FlashbackCast("{6}{G}{G}"));
    }
}
