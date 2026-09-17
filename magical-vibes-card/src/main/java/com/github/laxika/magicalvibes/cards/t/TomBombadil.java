package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlledPermanentCounterTotalAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "38")
@CardRegistration(set = "HOC", collectorNumber = "78")
public class TomBombadil extends Card {

    public TomBombadil() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlledPermanentCounterTotalAtLeast(
                        4, CounterType.LORE, new PermanentHasSubtypePredicate(CardSubtype.SAGA)),
                new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.SELF)));

        addEffect(EffectSlot.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES,
                new OncePerTurnTriggerEffect(new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardSubtypePredicate(CardSubtype.SAGA), LibrarySearchDestination.BATTLEFIELD)));
    }
}
