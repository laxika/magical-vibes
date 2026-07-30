package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M12", collectorNumber = "108")
public class Smallpox extends Card {

    public Smallpox() {
        // The four steps happen in order for all players: life loss, discard, creature
        // sacrifice, then land sacrifice.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
