package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "278")
public class SterlingGrove extends Card {

    public SterlingGrove() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.OWN_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(new CardTypePredicate(CardType.ENCHANTMENT),
                                LibrarySearchDestination.TOP_OF_LIBRARY)
                ),
                "{1}, Sacrifice Sterling Grove: Search your library for an enchantment card, reveal it, then shuffle and put that card on top."
        ));
    }
}
