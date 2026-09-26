package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

/**
 * Of Herbs and Stewed Rabbit - {2}{W} Enchantment - Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I - Put a +1/+1 counter on up to one target creature. Create a Food token.
 * II - Draw a card. Create a Food token.
 * III - Create a 1/1 white Halfling creature token for each Food you control.
 */
@CardRegistration(set = "LTC", collectorNumber = "17")
public class OfHerbsAndStewedRabbit extends Card {

    public OfHerbsAndStewedRabbit() {
        // Chapter I: Put a +1/+1 counter on up to one target creature, then create a Food token.
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_I, CreateTokenEffect.ofFoodToken(1));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        // Chapter II: Draw a card, then create a Food token.
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DrawCardEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofFoodToken(1));

        // Chapter III: Create one 1/1 white Halfling for each Food you control.
        PermanentCount foodsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOOD), CountScope.CONTROLLER);
        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenEffect(
                foodsYouControl, "Halfling", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HALFLING), Set.of(), Set.of()));
    }
}
