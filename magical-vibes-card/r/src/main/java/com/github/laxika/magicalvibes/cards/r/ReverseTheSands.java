package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedistributePlayerLifeTotalsEffect;

@CardRegistration(set = "CHK", collectorNumber = "41")
@CardRegistration(set = "C16", collectorNumber = "75")
public class ReverseTheSands extends Card {

    public ReverseTheSands() {
        addSpellEffect(new RedistributePlayerLifeTotalsEffect());
    }
}
