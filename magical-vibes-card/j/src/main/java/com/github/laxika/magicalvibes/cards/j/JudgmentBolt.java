package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "559")
public class JudgmentBolt extends Card {

    public JudgmentBolt() {
        PermanentCount equipmentCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER);
        addSimultaneousSpellEffects(DealDamageEffect.toTargetCreature(5),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), equipmentCount));
    }
}
