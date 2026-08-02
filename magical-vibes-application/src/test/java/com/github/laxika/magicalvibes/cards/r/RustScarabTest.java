package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RustScarabTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new RustScarab());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
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
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting destroys the chosen artifact defending player controls")
    void acceptDestroysArtifact() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.addToBattlefield(player2, new FountainOfYouth());

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Fountain of Youth"));

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

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));

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
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }
}
