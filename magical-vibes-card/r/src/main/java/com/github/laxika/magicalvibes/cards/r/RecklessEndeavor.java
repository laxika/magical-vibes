package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RollTwoDiceChooseOneEffect;

@CardRegistration(set = "SLD", collectorNumber = "2177")
@CardRegistration(set = "AFC", collectorNumber = "33")
public class RecklessEndeavor extends Card {

    public RecklessEndeavor() {
        addSpellEffect(new RollTwoDiceChooseOneEffect(12));
        addSpellEffect(new MassDamageEffect(new XValue(), false));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1).withAmount(new EventValue()));
    }
}
