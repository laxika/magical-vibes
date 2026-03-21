package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumAttackersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "DOM", collectorNumber = "150")
public class WarcryPhoenix extends Card {

    public WarcryPhoenix() {
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK,
                new MinimumAttackersConditionalEffect(3,
                        new MayPayManaEffect("{2}{R}",
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .enterTapped(true)
                                        .enterAttacking(true)
                                        .build(),
                                "Pay {2}{R} to return Warcry Phoenix from your graveyard to the battlefield tapped and attacking?"
                        )
                )
        );
    }
}
