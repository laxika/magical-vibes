package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HBG", collectorNumber = "25")
public class PatriarsHumiliation extends Card {

    public PatriarsHumiliation() {
        addSpellEffect(new PerpetuallyGrantStaticEffectToTargetCreatureEffect(
                new LosesAllAbilitiesEffect(Subjects.source())));
        addSpellEffect(DealDamageEffect.toTargetCreature(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
