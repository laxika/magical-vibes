package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantSearchLibrariesThisTurnEffect;

@CardRegistration(set = "RAV", collectorNumber = "253")
@CardRegistration(set = "SLD", collectorNumber = "414")
public class ShadowOfDoubt extends Card {

    public ShadowOfDoubt() {
        addSpellEffect(new PlayersCantSearchLibrariesThisTurnEffect());
        addSpellEffect(new DrawCardEffect(1));
    }
}
