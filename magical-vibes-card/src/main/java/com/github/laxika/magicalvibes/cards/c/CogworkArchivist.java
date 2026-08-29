package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "254")
public class CogworkArchivist extends Card {

    public CogworkArchivist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(
                        PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.BOTTOM)),
                "{2}, {T}: Put target card from a graveyard on the bottom of its owner's library."
        ));
    }
}
