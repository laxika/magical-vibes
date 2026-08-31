package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.ExpensiveTaste;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "WOE", collectorNumber = "223")
public class DecadentDragonExpensiveTaste extends Card {

    public DecadentDragonExpensiveTaste() {
        setBackFaceCard(new ExpensiveTaste());
        addCastingOption(new AdventureCast("{2}{B}"));
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "ExpensiveTaste";
    }
}
