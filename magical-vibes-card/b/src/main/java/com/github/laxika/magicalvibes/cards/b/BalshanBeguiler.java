package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

@CardRegistration(set = "ODY", collectorNumber = "66")
public class BalshanBeguiler extends Card {

    public BalshanBeguiler() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LookAtTopCardsOfTargetLibraryEffect(2,
                        TargetLibraryAction.REVEAL_AND_PUT_ONE_INTO_GRAVEYARD));
    }
}
