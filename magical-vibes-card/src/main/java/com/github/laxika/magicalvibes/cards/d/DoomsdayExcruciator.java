package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllButBottomCardsOfEachLibraryFaceDownEffect;

@CardRegistration(set = "DSK", collectorNumber = "94")
public class DoomsdayExcruciator extends Card {

    public DoomsdayExcruciator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new WasCast(),
                new ExileAllButBottomCardsOfEachLibraryFaceDownEffect(6)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawCardEffect());
    }
}
