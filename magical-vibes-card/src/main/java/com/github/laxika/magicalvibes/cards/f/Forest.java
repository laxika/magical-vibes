package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class Forest extends Card {

    public Forest() {
        super("Forest", "Basic Land", "Forest", "G", List.of(new AwardManaEffect("G")), null, null, null);
    }
}
