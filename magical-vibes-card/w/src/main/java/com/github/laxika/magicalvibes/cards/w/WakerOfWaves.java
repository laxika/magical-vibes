package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "84")
public class WakerOfWaves extends Card {

    public WakerOfWaves() {
        addStaticEffect(new StaticBoostEffect(-1, 0, GrantScope.OPPONENT_CREATURES));
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1)),
                "{1}{U}, Discard this card: Look at the top two cards of your library. Put one of them into your hand and the other into your graveyard."));
    }
}
