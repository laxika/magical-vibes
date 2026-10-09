package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C17", collectorNumber = "3")
public class CurseOfVitality extends Card {

    public CurseOfVitality() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        SequenceEffect.of(
                                new GainLifeEffect(2),
                                new ConditionalEffect(
                                        new AllOf(java.util.List.of(
                                                new AttackingPlayerIsOpponent(), new AttacksEnchantedPlayer())),
                                        new GainLifeEffect(new Fixed(2), GainLifeRecipient.TRIGGERING_PLAYER)))));
    }
}
