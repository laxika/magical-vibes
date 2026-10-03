package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.cards.o.OgreErrant;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
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

@CardUsed({BelovedPrincess.class, GarenbrigSquire.class, OgreErrant.class, RovingKeep.class})
class BelovedPrincessTest extends BaseCardTest {

    @Test
    @DisplayName("Beloved Princess can't be blocked by a creature with power 3")
    void cannotBeBlockedByPowerThree() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OgreErrant());

        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, princess))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Beloved Princess can be blocked by a creature with power 2")
    void canBeBlockedByPowerTwo() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GarenbrigSquire());

        beginDeclareBlockers();
        declareBlock(blocker, princess);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lifelink gains life from combat damage to a player")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPrincess();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void cannotBeBlockedByPowerGreaterThanThree() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, princess))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blockerWithPrintedPowerTwoCannotBlockAfterPowerIncrease() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GarenbrigSquire());
        blocker.setPersistentPowerModifier(1);
        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, princess))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blockerWithPrintedPowerThreeCanBlockAfterPowerReduction() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OgreErrant());
        blocker.setPersistentPowerModifier(-1);
        beginDeclareBlockers();
        declareBlock(blocker, princess);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void lifelinkGainsLifeFromDamageToBlockerEvenWhenPrincessDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GarenbrigSquire());
        beginDeclareBlockers();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> declareBlock(blocker, princess));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Beloved Princess");
        harness.assertOnBattlefield(player2, "Garenbrig Squire");
    }

    private Permanent addPrincess() {
        Permanent princess = harness.addToBattlefieldAndReturn(player1, new BelovedPrincess());
        princess.setSummoningSick(false);
        return princess;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
