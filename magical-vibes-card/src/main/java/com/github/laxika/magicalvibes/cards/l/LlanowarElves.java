package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

public class LlanowarElves extends Card {

    public LlanowarElves() {
        super("Llanowar Elves", CardType.CREATURE, List.of(CardSubtype.ELF, CardSubtype.DRUID), "G", List.of(new AwardManaEffect("G")), "{G}", 1, 1, Set.of());
    }
}
