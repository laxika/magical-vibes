package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DTK", collectorNumber = "153")
public class SarkhansRage extends Card {

    public SarkhansRage() {
        addSpellEffect(DealDamageEffect.toAnyTarget(5));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                new DealDamageToPlayersEffect(2, Subjects.you())));
    }
}
