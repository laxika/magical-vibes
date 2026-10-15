package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFC", collectorNumber = "82")
@CardRegistration(set = "C17", collectorNumber = "9")
public class CurseOfVerbosity extends Card {

    public CurseOfVerbosity() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                new ConditionalEffect(new AllOf(java.util.List.of(new AttackingPlayerIsOpponent(), new AttacksEnchantedPlayer())), new DrawCardForTriggeringPlayerEffect()))));
    }
}
