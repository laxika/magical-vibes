package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "28")
public class TajicLegionsValor extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Boros Recruit",
            "Swiftblade Vindicator",
            "Skyknight Legionnaire",
            "Spark Trooper",
            "Aurelia, the Law Above");

    public TajicLegionsValor() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
                        SPELLBOOK, new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TOKENS_CREATED_THIS_RESOLUTION),
                new MakeCreatedPermanentsMustAttackThisCombatEffect()));
    }
}
