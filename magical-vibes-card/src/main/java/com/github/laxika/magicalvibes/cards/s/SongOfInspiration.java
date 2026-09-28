package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.TargetCardsManaValueSum;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "AFC", collectorNumber = "42")
public class SongOfInspiration extends Card {

    public SongOfInspiration() {
        ReturnTargetCardsFromGraveyardToHandEffect returnCards =
                new ReturnTargetCardsFromGraveyardToHandEffect(new CardIsPermanentPredicate(), 2);

        target(new GraveyardCardPredicateTargetFilter(
                new CardIsPermanentPredicate(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 2)
                .addEffect(EffectSlot.SPELL, RollD20Effect.withAddedAmount(
                        new TargetCardsManaValueSum(),
                        returnCards,
                        SequenceEffect.of(
                                returnCards,
                                new GainLifeEffect(new TargetCardsManaValueSum()))));
    }
}
