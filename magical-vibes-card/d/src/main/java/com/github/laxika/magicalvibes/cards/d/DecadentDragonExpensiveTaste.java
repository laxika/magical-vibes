package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.ExpensiveTaste;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "223")
@CardRegistration(set = "WOE", collectorNumber = "287")
public class DecadentDragonExpensiveTaste extends Card {

    public DecadentDragonExpensiveTaste() {
        setBackFaceCard(new ExpensiveTaste());
        addCastingOption(new AdventureCast("{2}{B}"));
        addTrigger(Triggers.attacks(), CreateTokenEffect.ofTreasureToken(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "ExpensiveTaste";
    }
}
