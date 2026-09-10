package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bullwhip;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritEnKor.class, Shock.class, SpinedWurm.class, Bullwhip.class})
class SpiritEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects only one damage from a two-damage event")
    void redirectsOnlyOneDamageFromLargerEvent() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());
        Permanent destination = addCreatureReady(player1, new SpinedWurm());

        harness.activateAbility(player1, indexOf(player1, spirit), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each activation redirects one damage")
    void multipleActivationsEachRedirectOneDamage() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());
        Permanent destination = addCreatureReady(player1, new SpinedWurm());

        harness.activateAbility(player1, indexOf(player1, spirit), null, destination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, spirit), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirects combat damage to a creature you control")
    void redirectsCombatDamage() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());
        Permanent destination = addCreatureReady(player1, new SpinedWurm());
        Permanent attacker = addCreatureReady(player2, new SpiritEnKor());

        harness.activateAbility(player1, indexOf(player1, spirit), null, destination.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, spirit), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());
        Permanent opponentCreature = addCreatureReady(player2, new SpinedWurm());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, spirit), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, spirit), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a controlled noncreature permanent")
    void cannotTargetControlledNoncreature() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, spirit), null, bullwhip.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The damage redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        Permanent spirit = addCreatureReady(player1, new SpiritEnKor());
        Permanent destination = addCreatureReady(player1, new SpinedWurm());

        harness.activateAbility(player1, indexOf(player1, spirit), null, destination.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
