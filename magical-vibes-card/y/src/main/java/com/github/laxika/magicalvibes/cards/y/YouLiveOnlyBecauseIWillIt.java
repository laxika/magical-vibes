package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedistributePlayerLifeTotalsEffect;

@CardRegistration(set = "DSC", collectorNumber = "362")
public class YouLiveOnlyBecauseIWillIt extends Card {

    public YouLiveOnlyBecauseIWillIt() {
        addSpellEffect(new RedistributePlayerLifeTotalsEffect());
    }
}
