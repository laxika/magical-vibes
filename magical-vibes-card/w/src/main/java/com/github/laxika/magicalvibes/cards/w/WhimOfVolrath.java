package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "TMP", collectorNumber = "102")
public class WhimOfVolrath extends Card {

    public WhimOfVolrath() {
        // Buyback {2} (You may pay an additional {2} as you cast this spell. If you do, put this
        // card into your hand as it resolves.)
        // Change the text of target permanent by replacing all instances of one color word with
        // another or one basic land type with another until end of turn.
        addEffect(EffectSlot.STATIC, new BuybackEffect("{2}"));
        // The buyback return is queued ahead of the text change: the text change suspends resolution
        // on a player choice, and effects queued behind a suspending effect are not resumed.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, true, false, true));
    }
}
