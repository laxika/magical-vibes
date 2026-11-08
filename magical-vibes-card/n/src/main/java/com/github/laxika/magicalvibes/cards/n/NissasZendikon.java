package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "197")
public class NissasZendikon extends Card {

    public NissasZendikon() {
        target(TargetFilters.land())
                .addStaticEffect(new EnchantedPermanentBecomesCreatureEffect(
                        4, 4, null, List.of(CardSubtype.ELEMENTAL)))
                .addStaticEffect(new GrantKeywordEffect(
                        Set.of(Keyword.REACH, Keyword.HASTE), GrantScope.ENCHANTED_PERMANENT));

        addTrigger(Triggers.creatureDies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
    }
}
