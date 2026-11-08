package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "55")
public class CorruptedZendikon extends Card {

    public CorruptedZendikon() {
        target(TargetFilters.land())
                .addStaticEffect(new EnchantedPermanentBecomesCreatureEffect(
                        3, 3, CardColor.BLACK, List.of(CardSubtype.OOZE)));

        addTrigger(Triggers.creatureDies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
    }
}
