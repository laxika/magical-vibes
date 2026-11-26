package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "44")
public class DrakeFamiliar extends Card {

    public DrakeFamiliar() {
        // When this creature enters, sacrifice it unless you return an enchantment to its owner's hand: any player's,
        // untargeted (its ruling).
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.anyPlayers(new PermanentIsEnchantmentPredicate(), "enchantment"),
                new SacrificeSelfEffect()));
    }
}
