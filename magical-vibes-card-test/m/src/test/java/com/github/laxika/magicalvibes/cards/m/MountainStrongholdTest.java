package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AwakeningOfVituGhazi;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.BartelRuneaxe;
import com.github.laxika.magicalvibes.cards.c.Chaoslace;
import com.github.laxika.magicalvibes.cards.l.LadyEvangela;
import com.github.laxika.magicalvibes.cards.t.TobiasAndrion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MountainStronghold.class, BartelRuneaxe.class, LadyEvangela.class, TobiasAndrion.class,
        BarbaryApes.class, AwakeningOfVituGhazi.class, Chaoslace.class})
class MountainStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("Red legendary creatures can band with other legendary creatures")
    void redLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new MountainStronghold());
        Permanent redLegendary = addCreatureReady(player1, new BartelRuneaxe());
        Permanent otherLegendary = addCreatureReady(player1, new LadyEvangela());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(redLegendary.getBandId()).isNotNull();
        assertThat(redLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new MountainStronghold());
        addCreatureReady(player1, new BartelRuneaxe());
        addCreatureReady(player1, new BarbaryApes());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("Non-red legendary creatures do not receive bands with other legendary creatures")
    void nonRedLegendaryCreaturesDoNotReceiveAbility() {
        harness.addToBattlefield(player1, new MountainStronghold());
        addCreatureReady(player1, new LadyEvangela());
        addCreatureReady(player1, new TobiasAndrion());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability applies only to legendary creatures controlled by Mountain Stronghold's controller")
    void abilityDoesNotApplyToOpponentsCreatures() {
        harness.addToBattlefield(player1, new MountainStronghold());
        addCreatureReady(player2, new BartelRuneaxe());
        addCreatureReady(player2, new LadyEvangela());

        beginAttackDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("An enabled band lets its controller assign a blocker's combat damage")
    void enabledBandLetsAttackingControllerAssignBlockerDamage() {
        harness.addToBattlefield(player1, new MountainStronghold());
        Permanent redLegendary = addCreatureReady(player1, new BartelRuneaxe());
        Permanent otherLegendary = addCreatureReady(player1, new LadyEvangela());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(otherLegendary.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherLegendary);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(redLegendary);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A red legendary creature can form a band by itself")
    void redLegendaryCanFormSingleCreatureBand() {
        harness.addToBattlefield(player1, new MountainStronghold());
        Permanent creature = addCreatureReady(player1, new BartelRuneaxe());

        declareBand(player1, List.of(1), List.of(List.of(1)));

        assertThat(creature.getBandId()).isNotNull();
    }

    @Test
    @DisplayName("A legendary blocking pair lets its controller assign the attacker's damage")
    void legendaryBlockingPairControlsAttackerDamage() {
        harness.addToBattlefield(player2, new MountainStronghold());
        Permanent attacker = addCreatureReady(player1, new TobiasAndrion());
        Permanent redLegendary = addCreatureReady(player2, new BartelRuneaxe());
        Permanent otherLegendary = addCreatureReady(player2, new LadyEvangela());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(4);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(otherLegendary.getId(), 4));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(redLegendary).doesNotContain(otherLegendary);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("An additional nonlegendary blocker does not disable the legendary pair's damage assignment")
    void additionalNonlegendaryBlockerDoesNotDisableDamageAssignment() {
        harness.addToBattlefield(player2, new MountainStronghold());
        Permanent attacker = addCreatureReady(player1, new TobiasAndrion());
        Permanent redLegendary = addCreatureReady(player2, new BartelRuneaxe());
        Permanent otherLegendary = addCreatureReady(player2, new LadyEvangela());
        Permanent nonlegendary = addCreatureReady(player2, new BarbaryApes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(4);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(nonlegendary.getId(), 4));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(redLegendary, otherLegendary).doesNotContain(nonlegendary);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("An existing band persists after Mountain Stronghold leaves, but damage assignment returns to the blocker controller")
    void removingStrongholdPreservesBandButRemovesDamageAssignmentAbility() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new MountainStronghold());
        Permanent redLegendary = addCreatureReady(player1, new BartelRuneaxe());
        Permanent otherLegendary = addCreatureReady(player1, new LadyEvangela());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stronghold));
        assertThat(redLegendary.getBandId()).isNotNull();
        assertThat(redLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.getBlockingTargetIds()).contains(redLegendary.getId(), otherLegendary.getId());
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(otherLegendary.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(redLegendary).doesNotContain(otherLegendary);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @CardUsed({MountainStronghold.class, LadyEvangela.class, AwakeningOfVituGhazi.class, Chaoslace.class})
    @DisplayName("Mountain Stronghold grants itself bands with other when it becomes a red legendary creature")
    void qualifyingStrongholdGrantsAbilityToItself() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new MountainStronghold());
        Permanent otherLegendary = addCreatureReady(player1, new LadyEvangela());
        harness.setHand(player1, List.of(new AwakeningOfVituGhazi(), new Chaoslace()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, stronghold.getId());
        harness.castAndResolveInstant(player1, 0, stronghold.getId());

        declareBand(player1, List.of(0, 1), List.of(List.of(0, 1)));

        assertThat(stronghold.getBandId()).isNotNull();
        assertThat(stronghold.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    private void beginAttackDeclaration(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player player, List<Integer> attackers, List<List<Integer>> bands) {
        beginAttackDeclaration(player);
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player, attackers, null, bands));
    }
}
