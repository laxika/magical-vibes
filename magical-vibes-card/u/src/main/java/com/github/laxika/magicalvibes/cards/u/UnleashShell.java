package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BRO", collectorNumber = "155")
public class UnleashShell extends Card {

    public UnleashShell() {
        addSimultaneousSpellEffects(DealDamageEffect.toTargetCreatureOrPlaneswalker(5),
                new DealDamageToPlayersEffect(2, Subjects.controllerOf(Subjects.target())));
    }
}
