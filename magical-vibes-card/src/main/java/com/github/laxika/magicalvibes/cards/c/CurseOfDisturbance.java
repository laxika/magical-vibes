package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C21", collectorNumber = "138")
@CardRegistration(set = "C17", collectorNumber = "16")
@CardRegistration(set = "SCD", collectorNumber = "74")
public class CurseOfDisturbance extends Card {

    public CurseOfDisturbance() {
        CreateTokenEffect zombie = CreateTokenEffect.blackZombie(1);
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        SequenceEffect.of(
                                zombie,
                                new ConditionalEffect(new AllOf(java.util.List.of(new AttackingPlayerIsOpponent(), new AttacksEnchantedPlayer())), new CreateTokenForTriggeringPlayerEffect(zombie)))));
    }
}
