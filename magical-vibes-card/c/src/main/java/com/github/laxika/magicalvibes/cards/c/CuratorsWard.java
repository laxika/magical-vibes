package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "49")
public class CuratorsWard extends Card {

    public CuratorsWard() {
        // Enchanted permanent has hexproof.
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ENCHANTED_PERMANENT));
        // When enchanted permanent leaves the battlefield, if it was historic, draw two cards.
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED),
                new EnchantedPermanentLeavesConditionalEffect(
                        new CardIsHistoricPredicate(),
                        List.of(new DrawCardEffect(2))
                ));
    }
}
