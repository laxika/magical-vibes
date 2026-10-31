package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "10E", collectorNumber = "240")
@CardRegistration(set = "TMP", collectorNumber = "207")
@CardRegistration(set = "INV", collectorNumber = "172")
@CardRegistration(set = "TPR", collectorNumber = "163")
public class Stun extends Card {

    public Stun() {
        addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET));
        addSpellEffect(new DrawCardEffect());
    }
}
