package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DKA", collectorNumber = "41")
public class IncreasingConfusion extends Card {

    public IncreasingConfusion() {
        // Target player mills X cards. If this spell was cast from a graveyard (flashback),
        // that player mills twice X cards instead.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new MillEffect(new XValue(), MillRecipient.TARGET_PLAYER),
                new MillEffect(new Scaled(new XValue(), 2), MillRecipient.TARGET_PLAYER)
        ));
        addCastingOption(new FlashbackCast("{X}{U}"));
    }
}
