package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "21")
@CardRegistration(set = "WHO", collectorNumber = "626")
public class TheGirlInTheFireplace extends Card {

    public TheGirlInTheFireplace() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Human Noble", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.NOBLE), Set.of(Keyword.VANISHING), Set.of(),
                Map.of(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)),
                        EffectSlot.UPKEEP_TRIGGERED,
                        new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME),
                        EffectSlot.STATIC,
                        new PreventAllDamageEffect())));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Horse", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.HORSE), Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new GrantKeywordEffect(
                        Keyword.HORSEMANSHIP, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.DOCTOR)))));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new RegisterDelayedCombatDamageEffect(new TimeTravelEffect(1)));
    }
}
