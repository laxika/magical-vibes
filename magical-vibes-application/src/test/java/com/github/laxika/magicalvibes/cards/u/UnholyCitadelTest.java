package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AdunOakenshield;
import com.github.laxika.magicalvibes.cards.e.ElvenRiders;
import com.github.laxika.magicalvibes.cards.j.Johan;
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

@CardUsed({UnholyCitadel.class, AdunOakenshield.class, Johan.class, ElvenRiders.class, TobiasAndrion.class})
class UnholyCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Black legendary creatures can band with other legendary creatures")
    void blackLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new UnholyCitadel());
        Permanent blackLegendary = addCreatureReady(player1, new AdunOakenshield());
        Permanent otherLegendary = addCreatureReady(player1, new Johan());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(blackLegendary.getBandId()).isNotNull();
        assertThat(blackLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new UnholyCitadel());
        addCreatureReady(player1, new AdunOakenshield());
        addCreatureReady(player1, new ElvenRiders());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("Nonblack legendary creatures do not receive the bands-with-other ability")
    void nonblackLegendariesCannotFormBand() {
        harness.addToBattlefield(player1, new UnholyCitadel());
        addCreatureReady(player1, new Johan());
        addCreatureReady(player1, new TobiasAndrion());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability does not apply to black legendary creatures controlled by an opponent")
    void abilityDoesNotApplyToOpponentsCreatures() {
        harness.addToBattlefield(player1, new UnholyCitadel());
        addCreatureReady(player2, new AdunOakenshield());
        addCreatureReady(player2, new Johan());

        beginAttackDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("An enabled band lets its controller assign a blocker's combat damage")
    void enabledBandLetsAttackingControllerAssignBlockerDamage() {
        harness.addToBattlefield(player1, new UnholyCitadel());
        Permanent blackLegendary = addCreatureReady(player1, new AdunOakenshield());
        Permanent otherLegendary = addCreatureReady(player1, new Johan());
        Permanent blocker = addCreatureReady(player2, new ElvenRiders());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(blackLegendary.getId(), otherLegendary.getId());
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blackLegendary.getId(), 3));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blackLegendary);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherLegendary);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    private void beginAttackDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player player, List<Integer> attackers, List<List<Integer>> bands) {
        beginAttackDeclaration(player);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player, attackers, null, bands));
    }
}
