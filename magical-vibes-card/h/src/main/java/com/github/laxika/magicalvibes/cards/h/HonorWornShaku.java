package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "254")
@CardRegistration(set = "DMC", collectorNumber = "187")
public class HonorWornShaku extends Card {

    public HonorWornShaku() {

        // Tap an untapped legendary permanent you control: Untap Honor-Worn Shaku.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new TapMultiplePermanentsCost(1, new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)),
                        new UntapPermanentsEffect(TapUntapScope.SELF)),
                "Tap an untapped legendary permanent you control: Untap Honor-Worn Shaku."
        ));
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
