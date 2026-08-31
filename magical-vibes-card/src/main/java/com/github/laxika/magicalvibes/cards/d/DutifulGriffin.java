package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "11")
public class DutifulGriffin extends Card {

    public DutifulGriffin() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsEnchantmentPredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{2}{W}, Sacrifice two enchantments: Return Dutiful Griffin from your graveyard to your hand."
        ));
    }
}
