package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachChosenColorSpellCastEffect;

@CardRegistration(set = "RTR", collectorNumber = "235")
public class TabletOfTheGuilds extends Card {

    public TabletOfTheGuilds() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect(2));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new GainLifeForEachChosenColorSpellCastEffect());
    }
}
