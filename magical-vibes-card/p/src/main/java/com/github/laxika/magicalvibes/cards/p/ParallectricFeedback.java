package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GPT", collectorNumber = "71")
public class ParallectricFeedback extends Card {

    public ParallectricFeedback() {
        addSpellEffect(new DealDamageToPlayersEffect(
                new TargetSpellManaValue(), Subjects.controllerOf(Subjects.targetSpell())));
    }
}
