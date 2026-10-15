package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "69")
public class TheWarInHeaven extends Card {

    public TheWarInHeaven() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new LoseLifeEffect(3, LoseLifeRecipient.CONTROLLER));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new MillEffect(3, MillRecipient.CONTROLLER));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), 3, false, false, null, 8,
                null, null, CounterType.NECRODERMIS, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new AnimateReturnedPermanentsEffect(
                new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null,
                        Set.of(CardType.ARTIFACT), GrantScope.TARGET, EffectDuration.PERMANENT, null)));
    }
}
