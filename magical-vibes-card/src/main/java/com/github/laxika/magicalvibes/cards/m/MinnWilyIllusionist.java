package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "16")
public class MinnWilyIllusionist extends Card {

    public MinnWilyIllusionist() {
        DynamicStaticBoostEffect illusionBoost = new DynamicStaticBoostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ILLUSION),
                        CountScope.CONTROLLER, true),
                new Fixed(0), GrantScope.SELF);

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new CreateTokenEffect(1, "Illusion", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.ILLUSION), Set.of(), Set.of(),
                        Map.of(EffectSlot.STATIC, illusionBoost)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ILLUSION),
                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                .boundedByEventValue()));
    }
}
