package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "OTJ", collectorNumber = "56")
public class MaraudingSphinx extends Card {

    public MaraudingSphinx() {
        addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                new OncePerTurnTriggerEffect(new SurveilEffect(2)));
    }
}
