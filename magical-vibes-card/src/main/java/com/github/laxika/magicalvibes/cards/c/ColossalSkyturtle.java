package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "216")
public class ColossalSkyturtle extends Card {

    public ColossalSkyturtle() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build()),
                "Channel — {2}{G}, Discard this card: Return target card from your graveyard to your hand."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "Channel — {1}{U}, Discard this card: Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));
    }
}
