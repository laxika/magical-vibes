package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "31")
public class SilverchaseFox extends Card {

    public SilverchaseFox() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new SacrificeSelfCost(), new ExileTargetPermanentEffect()),
                "{1}{W}, Sacrifice Silverchase Fox: Exile target enchantment.",
                TargetFilters.enchantment()
        ));
    }
}
