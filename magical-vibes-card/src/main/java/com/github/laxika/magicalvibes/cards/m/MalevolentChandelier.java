package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "252")
public class MalevolentChandelier extends Card {

    public MalevolentChandelier() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(
                        PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.BOTTOM)),
                "{2}: Put target card from a graveyard on the bottom of its owner's library. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
