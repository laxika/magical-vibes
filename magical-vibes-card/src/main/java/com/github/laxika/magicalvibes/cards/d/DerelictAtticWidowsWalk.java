package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "93")
public class DerelictAtticWidowsWalk extends Card {

    public DerelictAtticWidowsWalk() {
        setRoomDoorManaCosts(List.of("{2}{B}", "{3}{B}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Derelict Attic", List.of())
                        .withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption("Widow's Walk", List.of())
                        .withManaCost("{3}{B}")
        )));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0,
                        SequenceEffect.of(new DrawCardEffect(2), new LoseLifeEffect(2))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                        new ConditionalEffect(new AttacksAlone(), SequenceEffect.of(
                                new BoostTargetCreatureEffect(1, 0),
                                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))),
                        GrantScope.SELF)));
    }
}
