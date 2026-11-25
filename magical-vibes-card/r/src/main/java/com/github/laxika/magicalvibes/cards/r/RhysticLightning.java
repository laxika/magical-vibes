package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "PCY", collectorNumber = "99")
public class RhysticLightning extends Card {

    public RhysticLightning() {
        addSpellEffect(UnlessPaysEffect.unlessPays(
                        Subjects.allOf(Subjects.controllerOf(Subjects.target()), Subjects.targetPlayerDeclaredElsewhere()),
                        "{2}", DealDamageEffect.toAnyTarget(4))
                .ifPaid(DealDamageEffect.toAnyTarget(2))
                .withPrompt("Pay {2} to reduce the damage to 2?"));
    }
}
