package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTransformedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "69")
public class OculusWhelp extends Card {

    public OculusWhelp() {
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentIsTransformedPredicate()),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(1)), GrantScope.SELF)));
    }
}
