package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

@CardRegistration(set = "GRN", collectorNumber = "205")
public class ThiefOfSanity extends Card {

    public ThiefOfSanity() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LookAtTopCardsOfTargetLibraryEffect(3,
                        TargetLibraryAction.EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD));
    }
}
