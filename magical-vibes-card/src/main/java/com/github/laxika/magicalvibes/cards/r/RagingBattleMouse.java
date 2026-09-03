package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSecondSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "143")
public class RagingBattleMouse extends Card {

    public RagingBattleMouse() {
        addEffect(EffectSlot.STATIC, new ReduceSecondSpellCastCostEffect(1));
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new PermanentEnteredThisTurn(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2),
                        new BoostTargetCreatureEffect(1, 1)));
    }
}
