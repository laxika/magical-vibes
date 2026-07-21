package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "123")
public class NissasDefeat extends Card {

    public NissasDefeat() {
        // Destroy target Forest, green enchantment, or green planeswalker. If that permanent was a
        // Nissa planeswalker, draw a card. The Nissa check runs on last-known state (pre-destruction).
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                                new PermanentIsPlaneswalkerPredicate()
                        ))
                )),
                "Target must be a Forest, green enchantment, or green planeswalker"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.NONE,
                new DrawCardEffect(1),
                ThenEffectRecipient.CONTROLLER,
                new PermanentHasSubtypePredicate(CardSubtype.NISSA)));
    }
}
