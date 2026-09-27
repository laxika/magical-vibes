package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "E02", collectorNumber = "4")
@CardRegistration(set = "SS2", collectorNumber = "5")
@CardRegistration(set = "SOC", collectorNumber = "172")
@CardRegistration(set = "C15", collectorNumber = "8")
public class ShieldedByFaith extends Card {

    public ShieldedByFaith() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_CREATURE));

        // Whenever a creature enters, you may attach this Aura to that creature.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new AttachSourceAuraToEnteringCreatureEffect());
    }
}
