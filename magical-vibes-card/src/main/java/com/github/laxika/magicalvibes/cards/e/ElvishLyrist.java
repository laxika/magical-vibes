package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "242")
@CardRegistration(set = "7ED", collectorNumber = "241")
@CardRegistration(set = "USG", collectorNumber = "248")
@CardRegistration(set = "BRB", collectorNumber = "23")
public class ElvishLyrist extends Card {

    public ElvishLyrist() {
        // {G}, {T}, Sacrifice this creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{G}, {T}, Sacrifice Elvish Lyrist: Destroy target enchantment.",
                TargetFilters.enchantment()
        ));
    }
}
