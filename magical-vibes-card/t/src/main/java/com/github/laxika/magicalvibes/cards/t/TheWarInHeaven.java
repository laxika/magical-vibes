package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "69")
public class TheWarInHeaven extends Card {

    public TheWarInHeaven() {
        addTrigger(Triggers.chapter(1), new DrawCardEffect(3), new LoseLifeEffect(3, LoseLifeRecipient.CONTROLLER));

        addTrigger(Triggers.chapter(2), new MillEffect(3, MillRecipient.CONTROLLER));

        addTrigger(Triggers.chapter(3),
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), 3, false, false, null, 8,
                        null, null, CounterType.NECRODERMIS, 1),
                new AnimateReturnedPermanentsEffect(
                        new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null,
                                Set.of(CardType.ARTIFACT), GrantScope.TARGET, EffectDuration.PERMANENT, null)));
    }
}
