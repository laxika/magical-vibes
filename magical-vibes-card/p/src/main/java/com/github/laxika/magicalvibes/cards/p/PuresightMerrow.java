package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "146")
public class PuresightMerrow extends Card {

    public PuresightMerrow() {
        // {W/U}, {Q}: Look at the top card of your library. You may exile that card.
        // No target = the controller's own library (handler falls back to controller).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W/U}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.MAY_EXILE_ONE)),
                "{W/U}, {Q}: Look at the top card of your library. You may exile that card."
        ).withRequiresUntap());
    }
}
