package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifeEqualToChosenCommanderManaValueEffect;

@CardRegistration(set = "SLD", collectorNumber = "2481")
@CardRegistration(set = "C21", collectorNumber = "44")
public class StingingStudy extends Card {

    public StingingStudy() {
        addEffect(EffectSlot.SPELL, new DrawAndLoseLifeEqualToChosenCommanderManaValueEffect());
    }
}
