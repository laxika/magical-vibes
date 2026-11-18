package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleRattleShaman.class, GrizzlyBears.class, Telepathy.class})
class BattleRattleShamanTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Accepting the may gives a target creature +2/+0")
    void acceptingMayBoostsTargetCreature() {
        harness.addToBattlefield(player1, new BattleRattleShaman());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the may leaves the target unchanged")
    void decliningMayLeavesTargetUnchanged() {
        harness.addToBattlefield(player1, new BattleRattleShaman());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("The trigger rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new BattleRattleShaman());
        Permanent telepathy = harness.addToBattlefieldAndReturn(player1, new Telepathy());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, telepathy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BattleRattleShaman());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("The trigger does not fire during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new BattleRattleShaman());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The Shaman can target itself even when it is the only creature")
    void canTargetItself() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BattleRattleShaman());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, shaman.getId());
        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, shaman)).isZero();
    }

    @Test
    @DisplayName("The controller may boost an opponent's creature")
    void canBoostOpponentsCreature() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BattleRattleShaman());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isZero();
    }
}
