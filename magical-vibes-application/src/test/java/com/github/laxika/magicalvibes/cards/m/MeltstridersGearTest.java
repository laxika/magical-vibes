package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeltstridersGear.class, GrizzlyBears.class})
class MeltstridersGearTest extends BaseCardTest {

    @Test
    @DisplayName("Meltstrider's Gear enters attached to a target creature and grants its bonuses")
    void entersAttachedAndGrantsBonuses() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeltstridersGear()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gear = findPermanent(player1, "Meltstrider's Gear");
        assertThat(gear.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Meltstrider's Gear enters unattached when its controller controls no creatures")
    void entersUnattachedWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new MeltstridersGear()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent gear = findPermanent(player1, "Meltstrider's Gear");
        assertThat(gear.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip attaches Meltstrider's Gear to another creature")
    void equipMovesGearToAnotherCreature() {
        Permanent gear = addEquipmentReady(player1);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        gear.setAttachedTo(firstBear.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, secondBear.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(secondBear.getId());
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondBear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.REACH)).isTrue();
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent gear = harness.addToBattlefieldAndReturn(player, new MeltstridersGear());
        gear.setSummoningSick(false);
        return gear;
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent gear = addEquipmentReady(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gear.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringOpponentsTurn() {
        Permanent gear = addEquipmentReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gear.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipConsumesFiveGenericMana() {
        Permanent gear = addEquipmentReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void failedEquipLeavesPreviousAttachmentAndBonusesIntact() {
        Permanent gear = addEquipmentReady(player1);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        gear.setAttachedTo(firstBear.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, secondBear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(secondBear);
        gd.playerGraveyards.get(player1.getId()).add(secondBear.getCard());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(firstBear.getId());
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.REACH)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
