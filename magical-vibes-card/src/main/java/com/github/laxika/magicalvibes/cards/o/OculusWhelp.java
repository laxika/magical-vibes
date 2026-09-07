package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTransformedPredicate;

@CardRegistration(set = "MOM", collectorNumber = "69")
public class OculusWhelp extends Card {

    public OculusWhelp() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentIsTransformedPredicate()),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_DEATH, new DrawCardEffect(1), GrantScope.SELF)));
    }
}
