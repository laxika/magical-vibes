package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchEngine.class, GorillaWarrior.class, Swamp.class, Rescind.class})
class WitchEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Adds four black mana and gives Witch Engine to the target opponent")
    void addsManaAndGivesControlToTargetOpponent() {
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(witchEngine.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Witch Engine");
        harness.assertNotOnBattlefield(player1, "Witch Engine");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addCreatureReady(player1, new WitchEngine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated during the opponent's turn")
    void canBeActivatedDuringOpponentsTurn() {
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(witchEngine.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot be blocked while the defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());
        witchEngine.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(witchEngine)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked while the defending player controls no Swamp")
    void canBeBlockedWhenDefenderDoesNotControlSwamp() {
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());
        witchEngine.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(witchEngine))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new WitchEngine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Cannot activate the tap ability twice without untapping")
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new WitchEngine());
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Still adds mana if Witch Engine is returned to hand in response")
    void addsManaWhenSourceLeavesBeforeResolution() {
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        harness.setHand(player2, List.of(new Rescind()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.castAndResolveInstant(player2, 0, witchEngine.getId());

        harness.assertInHand(player1, "Witch Engine");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Witch Engine");
    }

    @Test
    @DisplayName("Control transfer removes Witch Engine from combat and keeps it tapped")
    void controlTransferRemovesAttackerFromCombat() {
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        witchEngine.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(witchEngine.isAttacking()).isFalse();
        assertThat(witchEngine.isTapped()).isTrue();
        assertThat(witchEngine.isSummoningSick()).isTrue();
        harness.assertOnBattlefield(player2, "Witch Engine");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Witch Engine");
        harness.assertNotOnBattlefield(player1, "Witch Engine");
    }
}
