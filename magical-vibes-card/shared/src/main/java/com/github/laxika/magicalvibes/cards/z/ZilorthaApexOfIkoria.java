package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class ZilorthaApexOfIkoria extends Card {

    public ZilorthaApexOfIkoria() {
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new AssignCombatDamageAsThoughUnblockedEffect(),
                Subjects.creaturesYouControl(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))));
    }
}
