package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksNotController;
import com.github.laxika.magicalvibes.model.effect.AttackingPlayerChoosesCreatureToBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LTC", collectorNumber = "62")
@CardRegistration(set = "LTC", collectorNumber = "144")
public class MirkwoodTrapper extends Card {

    public MirkwoodTrapper() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_CREATURES_ATTACK_YOU,
                        new BoostTargetCreatureEffect(-2, 0));

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new PlayerAttacksNotController(),
                        new AttackingPlayerChoosesCreatureToBoostEffect(2, 0)));
    }
}
