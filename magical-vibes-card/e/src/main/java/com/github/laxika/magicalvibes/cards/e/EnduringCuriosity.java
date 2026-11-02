package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "51")
public class EnduringCuriosity extends Card {

    public EnduringCuriosity() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(null, new DrawCardEffect(1)));
        addTrigger(Triggers.dies(Triggers.CREATURE, ObjectRelation.THIS),
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT)));
    }
}
