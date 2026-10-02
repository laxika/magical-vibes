package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSNC", collectorNumber = "14")
public class LooseInThePark extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Exuberant Wolfbear",
            "Master Symmetrist",
            "Thrashing Brontodon",
            "Ornery Dilophosaur",
            "Prized Unicorn",
            "Sabertooth Mauler",
            "Spike-Tailed Ceratops",
            "Bristling Boar",
            "Enraged Ceratok",
            "Spore Crawler",
            "Predatory Wurm",
            "Gaea's Protector",
            "Wardscale Crocodile",
            "Overgrown Armasaur",
            "World Shaper");

    public LooseInThePark() {
        target(TargetFilters.land());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DrawCardEffect(),
                new DraftCardFromSpellbookToExileEffect(SPELLBOOK, false)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect(
                                Set.of(CardType.LAND)),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_PERMANENT)),
                "Enchanted land becomes a copy of the exiled card until end of turn and gains haste. "
                        + "It's still a land."));
    }
}
