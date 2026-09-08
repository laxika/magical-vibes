package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "158")
public class TicketBoothTunnelOfHate extends Card {

    public TicketBoothTunnelOfHate() {
        setRoomDoorManaCosts(List.of("{2}{R}", "{4}{R}{R}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Ticket Booth", List.of())
                        .withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption("Tunnel of Hate", List.of())
                        .withManaCost("{4}{R}{R}")
        )));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0, ManifestDreadEffect.forController()));

        PermanentAllOfPredicate attackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET, attackingCreature),
                        GrantScope.SELF)));
    }
}
