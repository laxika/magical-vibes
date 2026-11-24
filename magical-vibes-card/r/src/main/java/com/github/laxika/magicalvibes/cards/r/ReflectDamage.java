package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseDamageSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MIR", collectorNumber = "277")
public class ReflectDamage extends Card {

    public ReflectDamage() {
        addSpellEffect(ChooseDamageSourceEffect.any());
        addSpellEffect(RedirectNextDamageFromChosenSourceToSelfEffect.toItsController(
                Subjects.thisWay(StepKind.CHOOSE)));
    }
}
