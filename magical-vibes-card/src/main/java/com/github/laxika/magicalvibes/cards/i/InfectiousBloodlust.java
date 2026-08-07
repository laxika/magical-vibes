package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "152")
public class InfectiousBloodlust extends Card {

    public InfectiousBloodlust() {
        // Enchant creature
        target(TargetFilters.creature())
        // Enchanted creature gets +2/+1, has haste, and attacks each combat if able.
        .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.ENCHANTED_CREATURE))
        .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE))
        .addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // When enchanted creature dies, you may search your library for a card named
        // Infectious Bloodlust, reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new MayEffect(
                new SearchLibraryEffect(new Fixed(1), new CardNamedPredicate("Infectious Bloodlust"),
                        LibrarySearchDestination.HAND),
                "Search your library for a card named Infectious Bloodlust?"
        ));
    }
}
