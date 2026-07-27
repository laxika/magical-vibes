package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;


@CardRegistration(set = "XLN", collectorNumber = "187")
public class DroverOfTheMighty extends Card {

    public DroverOfTheMighty() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)), new StaticBoostEffect(2, 2, GrantScope.SELF)));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
