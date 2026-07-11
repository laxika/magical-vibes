package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "137")
public class HuaTuoHonoredPhysician extends Card {

    public HuaTuoHonoredPhysician() {
        // {T}: Put target creature card from your graveyard on top of your library.
        // Activate only during your turn, before attackers are declared.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()),
                "{T}: Put target creature card from your graveyard on top of your library. "
                        + "Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED
        ));
    }
}
