package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "224")
@CardRegistration(set = "AKR", collectorNumber = "278")
public class PerilousVault extends Card {

    public PerilousVault() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new ExileSelfCost(),
                        new ExileAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsLandPredicate()))),
                "{5}, {T}, Exile Perilous Vault: Exile all nonland permanents."
        ));
    }
}
