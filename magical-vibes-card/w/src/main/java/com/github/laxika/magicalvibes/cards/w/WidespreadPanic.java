package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C13", collectorNumber = "131")
public class WidespreadPanic extends Card {

    public WidespreadPanic() {
        addTrigger(Triggers.spellOrAbilityCausesItsControllerToShuffle(),
                new TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(1));
    }
}
