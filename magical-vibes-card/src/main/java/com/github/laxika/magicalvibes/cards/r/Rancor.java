package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "185")
public class Rancor extends Card {

    public Rancor() {
        target(TargetFilters.creature())
                // Enchanted creature gets +2/+0 and has trample.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        2, 0, Set.of(Keyword.TRAMPLE), GrantScope.ENCHANTED_CREATURE))
                // When this Aura is put into a graveyard from the battlefield, return it to its owner's hand.
                .addEffect(EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
