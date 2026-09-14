package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GN3", collectorNumber = "4")
public class NogiDracoZealot extends Card {

    public NogiDracoZealot() {
        // Dragon spells you cast cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON), 1, CostModificationScope.SELF));

        // Whenever Nogi attacks, if you control three or more Dragons, until end of turn, Nogi
        // becomes a Dragon with base power and toughness 5/5 and gains flying.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                SequenceEffect.of(
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON),
                        new AnimatePermanentsEffect(5, 5, List.of(), Set.of(Keyword.FLYING)))));
    }
}
