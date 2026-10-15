package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "SOS", collectorNumber = "202")
@CardRegistration(set = "SOS", collectorNumber = "352")
public class MindIntoMatter extends Card {

    public MindIntoMatter() {
        // Draw X cards. Then you may put a permanent card with mana value X or less
        // from your hand onto the battlefield tapped.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent", true, true),
                "Put a permanent card with mana value X or less from your hand onto the battlefield tapped?"
        ));
    }
}
