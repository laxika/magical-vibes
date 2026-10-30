package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "28")
public class TajicLegionsValor extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "243"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "203"),
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "232"),
            new DraftFromSpellbookEffect.SpellbookCard("GTC", "199"),
            new DraftFromSpellbookEffect.SpellbookCard("MKM", "188"));

    public TajicLegionsValor() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
                        SPELLBOOK, new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TOKENS_CREATED_THIS_RESOLUTION),
                new MakeCreatedPermanentsMustAttackThisCombatEffect()));
    }
}
