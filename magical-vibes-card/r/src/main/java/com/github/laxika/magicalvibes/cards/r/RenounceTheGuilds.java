package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "DGM", collectorNumber = "5")
public class RenounceTheGuilds extends Card {

    public RenounceTheGuilds() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentIsMulticoloredPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
