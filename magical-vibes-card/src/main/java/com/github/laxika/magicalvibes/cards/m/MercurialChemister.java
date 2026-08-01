package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "180")
public class MercurialChemister extends Card {

    public MercurialChemister() {
        // {U}, {T}: Draw two cards.
        addActivatedAbility(new ActivatedAbility(true, "{U}",
                List.of(new DrawCardEffect(2)),
                "{U}, {T}: Draw two cards."));

        // {R}, {T}, Discard a card: deals damage to target creature equal to the discarded card's mana value.
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(
                        new DiscardCardTypeCost(null, null, false, 1, false, true),
                        new DealDamageToTargetCreatureEffect(new XValue())),
                "{R}, {T}, Discard a card: This creature deals damage to target creature equal to the discarded card's mana value.",
                TargetFilters.creature()));
    }
}
