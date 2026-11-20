package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MBS", collectorNumber = "60")
public class ConcussiveBolt extends Card {

    public ConcussiveBolt() {
        addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(4));
        addSpellEffect(new ConditionalEffect(new Metalcraft(),
                new CantBlockThisTurnEffect(Subjects.controlledBy(
                        Subjects.allOf(Subjects.targetPlayerDeclaredElsewhere(), Subjects.controllerOf(Subjects.target()))))));
    }
}
