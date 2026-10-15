package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "36")
public class WojekApothecary extends Card {

    public WojekApothecary() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTargetAndSharingCreatures(1)),
                "{T}: Prevent the next 1 damage that would be dealt to target creature and each other creature that shares a color with it this turn.",
                TargetFilters.creature()
        ));
    }
}
