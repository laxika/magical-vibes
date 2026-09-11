package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ChooseEquipmentToUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "220")
public class AkiriFearlessVoyager extends Card {

    public AkiriFearlessVoyager() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new ConditionalEffect(
                        new HasAttacker(new PermanentIsEquippedPredicate()),
                        new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new ChooseEquipmentToUnattachEffect()),
                "{W}: You may unattach an Equipment from a creature you control. "
                        + "If you do, tap that creature and it gains indestructible until end of turn."));
    }
}
