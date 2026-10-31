package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "SNC", collectorNumber = "72")
public class CutOfTheProfits extends Card {

    public CutOfTheProfits() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect());
        addCastingCost(new CasualtyCost(3));
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER));
    }
}
