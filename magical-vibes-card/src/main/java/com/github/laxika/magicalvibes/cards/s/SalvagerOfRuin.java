package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "237")
public class SalvagerOfRuin extends Card {

    public SalvagerOfRuin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsPermanentPredicate())
                                .targetGraveyard(true)
                                .targetPutIntoGraveyardFromBattlefieldThisTurn(true)
                                .build()
                ),
                "Sacrifice this creature: Choose target permanent card in your graveyard that was put there from the battlefield this turn. Return it to your hand."
        ));
    }
}
