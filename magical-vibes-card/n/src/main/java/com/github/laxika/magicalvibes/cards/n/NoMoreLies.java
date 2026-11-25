package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "MKM", collectorNumber = "221")
@CardRegistration(set = "MKM", collectorNumber = "427")
public class NoMoreLies extends Card {

    public NoMoreLies() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(3).exilingIfCountered());
    }
}
