package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives +2/+2 to creatures you control only")
    void boostsOwnCreaturesOnly() {
        Permanent source = addReadyMaster(player1);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(source.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownBears.getEffectivePower()).isEqualTo(4);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingBears.getEffectivePower()).isEqualTo(2);
        assertThat(opposingBears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent source = addReadyMaster(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, null);
        harness.passBothPriorities();
        assertThat(source.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        Permanent source = addReadyMaster(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Second ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent source = addReadyMaster(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                1,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMaster(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SunscapeMaster());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
