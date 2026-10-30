package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSNC", collectorNumber = "14")
public class LooseInThePark extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "151"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "138"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "148"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "194"),
            new DraftFromSpellbookEffect.SpellbookCard("M10", "199"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "202"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "209"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "170"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "125"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "222"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "338"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "162"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "183"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "141"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "151"));

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
