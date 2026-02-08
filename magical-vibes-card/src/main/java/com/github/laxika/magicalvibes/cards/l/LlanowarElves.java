package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class LlanowarElves extends Card {

    public LlanowarElves() {
        super("Llanowar Elves", "Creature", "Elf Druid", "G", List.of(new AwardManaEffect("G")), "{G}", 1, 1);
    }
}
