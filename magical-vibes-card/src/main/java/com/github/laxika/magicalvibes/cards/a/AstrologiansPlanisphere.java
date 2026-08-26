package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "46")
public class AstrologiansPlanisphere extends Card {

    public AstrologiansPlanisphere() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LivingWeaponEffect(new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of())));

        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.WIZARD, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                        GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_CONTROLLER_DRAWS,
                        new OncePerTurnTriggerEffect(new ConditionalEffect(
                                new ControllerDrewAtLeastCardsThisTurn(3),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                        GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
