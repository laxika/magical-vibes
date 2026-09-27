package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BartelRuneaxe;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.l.LadyEvangela;
import com.github.laxika.magicalvibes.cards.t.TobiasAndrion;
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
        BarbaryApes.class})
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
