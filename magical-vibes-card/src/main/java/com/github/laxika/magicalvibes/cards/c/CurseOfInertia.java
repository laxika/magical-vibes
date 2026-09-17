package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;

@CardRegistration(set = "C13", collectorNumber = "36")
public class CurseOfInertia extends Card {

    public CurseOfInertia() {
        addEffect(EffectSlot.ON_CREATURES_ATTACK_YOU, new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap target permanent?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
