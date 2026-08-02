package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "71")
public class KamiOfTwistedReflection extends Card {

    public KamiOfTwistedReflection() {
        // Sacrifice this creature: Return target creature you control to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), ReturnToHandEffect.target()),
                "Sacrifice Kami of Twisted Reflection: Return target creature you control to its owner's hand.",
                TargetFilters.creatureYouControl()
        ));
    }
}
