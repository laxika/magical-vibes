package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DefendingPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsDownToHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "266")
@CardRegistration(set = "MB2", collectorNumber = "502")
public class KozilekCompleated extends Card {

    public KozilekCompleated() {
        addTrigger(Triggers.youCastThisSpell(),
                new GivePoisonCountersEffect(2, PoisonRecipient.EACH_OPPONENT));
        addTrigger(Triggers.youCastThisSpell(), new EachOpponentDiscardsDownToHandSizeEffect(2));
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                new DefendingPlayerPoisonCounters(), new PermanentTruePredicate(),
                SacrificeRecipient.DEFENDING_PLAYER));
    }
}
