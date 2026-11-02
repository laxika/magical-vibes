package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "47")
public class ALittleChat extends Card {

    public ALittleChat() {
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfCasualtyPaidEffect());
        addCastingCost(new CasualtyCost(1));
        addSpellEffect(LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2)));
    }
}
