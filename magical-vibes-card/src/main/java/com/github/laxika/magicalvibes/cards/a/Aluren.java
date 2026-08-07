package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Aluren — "Any player may cast creature spells with mana value 3 or less without paying their mana
 * costs and as though they had flash."
 *
 * <p>Both halves are all-player static permissions keyed off the same predicate: a zero alternative
 * cost ({@code {0}}, any zone) and a flash timing grant.
 */
@CardRegistration(set = "TMP", collectorNumber = "213")
public class Aluren extends Card {

    public Aluren() {
        CardPredicate smallCreatureSpell = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(3)));

        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{0}", smallCreatureSpell, null, false, false, true));
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(smallCreatureSpell, true));
    }
}
