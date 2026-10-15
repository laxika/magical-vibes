package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "238")
public class DruidLyrist extends Card {

    public DruidLyrist() {
        // {G}, {T}, Sacrifice this creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{G}, {T}, Sacrifice Druid Lyrist: Destroy target enchantment.",
                TargetFilters.enchantment()
        ));
    }
}
