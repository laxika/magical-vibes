package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BOK", collectorNumber = "100")
public class FirstVolley extends Card {

    public FirstVolley() {
        addSimultaneousSpellEffects(DealDamageEffect.toTargetCreature(1),
                new DealDamageToPlayersEffect(1, Subjects.controllerOf(Subjects.target())));
    }
}
