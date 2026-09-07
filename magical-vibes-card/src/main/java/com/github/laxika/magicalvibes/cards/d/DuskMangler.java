package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureOrDiscardCardOrPayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SNC", collectorNumber = "77")
public class DuskMangler extends Card {

    public DuskMangler() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureOrDiscardCardOrPayLifeCost(4));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new SacrificePermanentsEffect(
                        1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                new LoseLifeEffect(4, LoseLifeRecipient.EACH_OPPONENT)));
    }
}
