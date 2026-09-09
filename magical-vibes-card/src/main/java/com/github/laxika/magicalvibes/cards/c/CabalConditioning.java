package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SCG", collectorNumber = "56")
public class CabalConditioning extends Card {

    public CabalConditioning() {
        target(0, 99).addEffect(EffectSlot.SPELL, new DiscardEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate()),
                DiscardRecipient.TARGET_PLAYER));
    }
}
