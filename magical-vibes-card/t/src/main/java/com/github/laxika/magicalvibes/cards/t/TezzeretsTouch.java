package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "138")
@CardRegistration(set = "KLR", collectorNumber = "210")
public class TezzeretsTouch extends Card {

    public TezzeretsTouch() {
        // Enchant artifact. Enchanted artifact is a 5/5 creature in addition to its other types.
        target(TargetFilters.artifact())
                .addStaticEffect(new EnchantedPermanentBecomesCreatureEffect(5, 5, null, List.of()))
                // When enchanted artifact is put into a graveyard, return that card to its owner's hand.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
    }
}
