package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

public class Forest extends Card {

    public Forest() {
        super("Forest", CardType.BASIC_LAND, List.of(CardSubtype.FOREST), "{T}: Add {G}.", List.of(new AwardManaEffect("G")), null, null, null, Set.of(), List.of(), List.of());
    }
}
