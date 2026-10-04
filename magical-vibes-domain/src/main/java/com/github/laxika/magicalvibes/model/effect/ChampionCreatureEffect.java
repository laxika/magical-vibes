package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import java.util.List;

/**
 * Champion a creature, or a permanent of one of the specified subtypes.
 * When this enters, sacrifice it unless you exile another matching permanent you control.
 * When this leaves the battlefield, the exiled card returns to the battlefield.
 * An empty {@code championedSubtypes} means any creature ("Champion a creature").
 * Multiple subtypes are matched inclusively ("Champion a Goblin or Shaman").
 */
public record ChampionCreatureEffect(List<CardSubtype> championedSubtypes) implements CardEffect {

    public ChampionCreatureEffect(CardSubtype... championedSubtypes) {
        this(List.of(championedSubtypes));
    }
}
