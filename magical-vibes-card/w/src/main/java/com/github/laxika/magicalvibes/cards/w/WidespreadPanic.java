package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;

@CardRegistration(set = "C13", collectorNumber = "131")
public class WidespreadPanic extends Card {

    public WidespreadPanic() {
        addEffect(EffectSlot.ON_OPPONENT_SHUFFLES_LIBRARY,
                new TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(1));
    }
}
