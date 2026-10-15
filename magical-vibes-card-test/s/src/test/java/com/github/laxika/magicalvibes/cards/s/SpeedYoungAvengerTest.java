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

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
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

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the payment leaves the target normally blockable")
    void decliningPaymentLeavesTargetNormallyBlockable() {
        Permanent target = prepareTrigger();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Targeting waits until payment and the reflexive trigger can be responded to")
    void paymentCreatesSeparateTargetedTrigger() {
        Permanent target = prepareTrigger();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(target.getBlockRestrictionsUntilEndOfTurn()).isEmpty();
        harness.passBothPriorities();
        assertThat(target.getBlockRestrictionsUntilEndOfTurn()).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Speed")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpeedYoungAvenger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The paid trigger can target an opponent's haste creature but not a creature without haste")
    void targetMustHaveHasteButMayBelongToOpponent() {
        prepareTrigger();
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent nonHaste = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(opponentTarget.getId()).doesNotContain(nonHaste.getId());
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();
        assertThat(opponentTarget.getBlockRestrictionsUntilEndOfTurn()).hasSize(1);

        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(opponentTarget.getBlockRestrictionsUntilEndOfTurn()).isEmpty();
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
