package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustScarab.class, GrizzlyBears.class, FountainOfYouth.class, AngelicChorus.class})
class RustScarabTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RustScarab());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private Permanent addBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        return blocker;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    @Test
    @DisplayName("Accepting destroys the chosen artifact defending player controls")
    void acceptDestroysArtifact() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.addToBattlefield(player2, new FountainOfYouth());

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Fountain of Youth"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Accepting destroys the chosen enchantment defending player controls")
    void acceptDestroysEnchantment() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.addToBattlefield(player2, new AngelicChorus());

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Declining leaves the artifact on the battlefield")
    void declineDestroysNothing() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.addToBattlefield(player2, new FountainOfYouth());

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Fountain of Youth"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Unblocked attacker does not trigger")
    void unblockedDoesNotTrigger() {
        addAttacker();
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("An artifact the attacking player controls is not a legal target")
    void ownArtifactIsNotALegalTarget() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.addToBattlefield(player1, new FountainOfYouth());

        declareBlock(attacker, blocker);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("An ordinary defending creature is not a legal target")
    void ordinaryCreatureIsNotALegalTarget() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The blocked trigger still resolves after Rust Scarab leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.addToBattlefield(player2, new FountainOfYouth());

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Fountain of Youth"));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The target becomes illegal if the attacking player gains control of it")
    void targetChangingControllerDoesNotResolve() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }
}
