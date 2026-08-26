package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class PhoenixWardenOfFire extends Card {

    public PhoenixWardenOfFire() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.withinTotalManaValue(
                        new CardTypePredicate(CardType.CREATURE), 6));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }
}
