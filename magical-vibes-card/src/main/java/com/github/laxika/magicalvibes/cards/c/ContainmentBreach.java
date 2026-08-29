package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "125")
public class ContainmentBreach extends Card {

    public ContainmentBreach() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        )).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentWithManaValueConditionalEffect(2, pestToken()));
    }

    private static CreateTokenEffect pestToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_DEATH, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of());
    }
}
