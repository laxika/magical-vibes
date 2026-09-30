package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEC", collectorNumber = "1")
@CardRegistration(set = "NEC", collectorNumber = "73")
@CardRegistration(set = "NEC", collectorNumber = "77")
public class ChishiroTheShatteredBlade extends Card {

    public ChishiroTheShatteredBlade() {
        CreateTokenEffect spiritToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Spirit", 2, 2, CardColor.RED,
                Set.of(CardColor.RED), List.of(CardSubtype.SPIRIT), Set.of(Keyword.MENACE), Set.of(),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardIsAuraPredicate(), spiritToken));
        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD, spiritToken);

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsModifiedPredicate()))));
    }
}
