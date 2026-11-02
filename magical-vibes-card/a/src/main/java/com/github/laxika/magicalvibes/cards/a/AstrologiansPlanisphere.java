package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "46")
@CardRegistration(set = "FIN", collectorNumber = "581")
public class AstrologiansPlanisphere extends Card {

    public AstrologiansPlanisphere() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LivingWeaponEffect(new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of())));

        addStaticEffect(new GrantSubtypeEffect(CardSubtype.WIZARD, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.youCastSpell(),
                        new SpellCastTriggerEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)))),
                        GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.youDrawCard(),
                        new NthCardDrawTriggerEffect(3,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                        GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
