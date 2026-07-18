package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NPH", collectorNumber = "44")
public class PsychicSurgery extends Card {

    public PsychicSurgery() {
        addEffect(EffectSlot.ON_OPPONENT_SHUFFLES_LIBRARY,
                new MayEffect(new LookAtTopCardsOfTargetLibraryEffect(2, TargetLibraryAction.MAY_EXILE_ONE),
                        "look at the top two cards of that library"));
    }
}
