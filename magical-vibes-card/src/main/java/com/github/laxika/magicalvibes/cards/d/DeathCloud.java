package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "40")
public class DeathCloud extends Card {

    public DeathCloud() {
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(new XValue(), LoseLifeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new DiscardEffect(new XValue(), DiscardRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new XValue(), new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new XValue(), new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
