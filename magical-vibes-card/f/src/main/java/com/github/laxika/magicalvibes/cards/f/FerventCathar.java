package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "135")
@CardRegistration(set = "EMA", collectorNumber = "129")
public class FerventCathar extends Card {

    public FerventCathar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
