package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "169")
public class LlanowarGreenwidow extends Card {

    public LlanowarGreenwidow() {
        // {7}{G}: Return this card from your graveyard to the battlefield tapped. It gains an
        // exile-if-it-leaves replacement effect. The ability costs {1} less for each basic land
        // type among lands you control.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{7}{G}",
                List.of(
                        new ReduceActivationCostEffect(new BasicLandTypesAmongControlledLands()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .exileIfLeavesBattlefield(true)
                                .build()
                ),
                "{7}{G}: Return this card from your graveyard to the battlefield tapped. It gains \"If "
                        + "this permanent would leave the battlefield, exile it instead of putting it "
                        + "anywhere else.\" This ability costs {1} less to activate for each basic land "
                        + "type among lands you control."
        ));
    }
}
