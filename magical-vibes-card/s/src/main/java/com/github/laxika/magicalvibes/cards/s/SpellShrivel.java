package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "BFZ", collectorNumber = "66")
public class SpellShrivel extends Card {

    public SpellShrivel() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(4).exilingIfCountered());
    }
}
