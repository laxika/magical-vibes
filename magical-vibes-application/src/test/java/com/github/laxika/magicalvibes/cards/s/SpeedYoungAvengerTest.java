package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpeedYoungAvenger.class, GrizzlyBears.class, LightningElemental.class,
        RagingGoblin.class, Shock.class})
class SpeedYoungAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} restricts the target creature's blockers to creatures with haste")
    void payingRestrictsBlockersToHasteCreatures() {
        Permanent target = prepareTrigger();
        Permanent normalBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hasteBlocker = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        int normalBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(normalBlocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(normalBlockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with haste");

        int hasteBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(hasteBlocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(hasteBlockerIndex, attackerIndex)));
        assertThat(hasteBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature spell does not trigger Speed")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpeedYoungAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the payment leaves the target normally blockable")
    void decliningPaymentLeavesTargetNormallyBlockable() {
        Permanent target = prepareTrigger();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent prepareTrigger() {
        harness.addToBattlefield(player1, new SpeedYoungAvenger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LightningElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return target;
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
