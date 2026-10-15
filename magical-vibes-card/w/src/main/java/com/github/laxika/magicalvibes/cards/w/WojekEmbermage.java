package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "152")
public class WojekEmbermage extends Card {

    public WojekEmbermage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetAndSharingCreaturesEffect(1)),
                "{T}: This creature deals 1 damage to target creature and each other creature that shares a color with it.",
                TargetFilters.creature()
        ));
    }
}
