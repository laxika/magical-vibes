package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextSpellOfTypeThisTurnEffect;

@CardRegistration(set = "FUT", collectorNumber = "16")
@CardRegistration(set = "TLE", collectorNumber = "6")
public class ScoutsWarning extends Card {

    public ScoutsWarning() {
        addSpellEffect(new GrantFlashToNextSpellOfTypeThisTurnEffect(CardType.CREATURE));
        addSpellEffect(new DrawCardEffect(1));
    }
}
