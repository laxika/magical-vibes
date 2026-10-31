package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "196")
@CardRegistration(set = "DBL", collectorNumber = "196")
public class RiseOfTheAnts extends Card {

    public RiseOfTheAnts() {
        addSpellEffect(new CreateTokenEffect(2, "Insect", 3, 3,
                CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(), Set.of()));
        addSpellEffect(new GainLifeEffect(2));
        addCastingOption(new FlashbackCast("{6}{G}{G}"));
    }
}
