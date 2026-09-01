package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "MKM", collectorNumber = "431")
public class TomikWielderOfLaw extends Card {

    public TomikWielderOfLaw() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsPlaneswalkerPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS, new ConditionalEffect(
                new OpponentAttacksWithAtLeastCreatures(2),
                SequenceEffect.of(
                        new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER),
                        new DrawCardEffect())));
    }
}
