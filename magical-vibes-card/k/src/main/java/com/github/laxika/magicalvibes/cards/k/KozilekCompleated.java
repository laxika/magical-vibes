package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DefendingPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsDownToHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "MB2", collectorNumber = "266")
@CardRegistration(set = "MB2", collectorNumber = "502")
public class KozilekCompleated extends Card {

    public KozilekCompleated() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new GivePoisonCountersEffect(2, PoisonRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_SELF_CAST, new EachOpponentDiscardsDownToHandSizeEffect(2));
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                new DefendingPlayerPoisonCounters(), new PermanentTruePredicate(),
                SacrificeRecipient.DEFENDING_PLAYER));
    }
}
