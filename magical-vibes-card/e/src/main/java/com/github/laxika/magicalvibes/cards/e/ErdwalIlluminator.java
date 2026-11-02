package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "60")
@CardRegistration(set = "SIR", collectorNumber = "66")
@CardRegistration(set = "MKC", collectorNumber = "104")
public class ErdwalIlluminator extends Card {

    public ErdwalIlluminator() {
        addTrigger(Triggers.youInvestigateFirstTimeEachTurn(), CreateTokenEffect.ofClueToken(1));
    }
}
