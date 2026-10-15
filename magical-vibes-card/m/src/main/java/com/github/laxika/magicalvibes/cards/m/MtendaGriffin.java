package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "28")
public class MtendaGriffin extends Card {

    public MtendaGriffin() {
        // {W}, {T}: Return this creature to its owner's hand and return target Griffin card
        // from your graveyard to your hand. Activate only during your upkeep.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        ReturnToHandEffect.self(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardSubtypePredicate(CardSubtype.GRIFFIN))
                                .targetGraveyard(true)
                                .build()
                ),
                "{W}, {T}: Return this creature to its owner's hand and return target Griffin card "
                        + "from your graveyard to your hand. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
