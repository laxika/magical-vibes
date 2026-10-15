package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "31")
public class SkybladesBoon extends Card {

    public SkybladesBoon() {
        // Enchant creature
        target(TargetFilters.creature());
        // Enchanted creature gets +1/+1 and has flying.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));

        // {2}{W}: Return Skyblade's Boon to its owner's hand. Activate only if Skyblade's Boon is
        // on the battlefield or in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(ReturnToHandEffect.self()),
                "{2}{W}: Return Skyblade's Boon to its owner's hand. Activate only if Skyblade's Boon "
                        + "is on the battlefield or in your graveyard."
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{W}: Return Skyblade's Boon to its owner's hand. Activate only if Skyblade's Boon "
                        + "is on the battlefield or in your graveyard."
        ));
    }
}
