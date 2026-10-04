package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcingdeathFrostTongue.class, GrizzlyBears.class})
class IcingdeathFrostTongueTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {2} attaches Frost Tongue and gives the creature +2/+0")
    void equipsAndBoostsCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, equipmentIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps a target creature defending player controls when the equipped creature attacks")
    void attackTriggerTapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the attacking player")
    void cannotTargetOwnCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void unattachedEquipmentDoesNotTriggerOrBoost() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new IcingdeathFrostTongue());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void reequippingMovesTheBoostToTheNewCreature() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void attackTriggerResolvesAfterEquipmentLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void attackTriggerStillResolvesAfterEquipmentBecomesUnattached() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void attackTriggerCanTargetAnAlreadyTappedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        victim.setTapped(true);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
