package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "JUD", collectorNumber = "41")
public class GripOfAmnesia extends Card {

    public GripOfAmnesia() {
        addSpellEffect(UnlessPaysEffect.counterUnlessExilesGraveyard());
        addSpellEffect(new DrawCardEffect());
    }
}
