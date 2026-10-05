package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianasSteward.class, WalkingCorpse.class, Duress.class})
class LilianasStewardTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Liliana's Steward sacrifices it")
    void activatingSacrificesSelf() {
        addReadySteward(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Liliana's Steward");
        harness.assertInGraveyard(player1, "Liliana's Steward");
    }

    @Test
    @DisplayName("Target opponent discards a card")
    void targetOpponentDiscards() {
        addReadySteward(player1);
        harness.setHand(player2, List.of(new WalkingCorpse(), new Duress()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Empty opponent hand causes no discard")
    void emptyHandCausesNoDiscard() {
        addReadySteward(player1);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        addReadySteward(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate at instant speed")
    void cannotActivateAtInstantSpeed() {
        addReadySteward(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A tapped Steward cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent steward = addReadySteward(player1);
        steward.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Liliana's Steward");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Steward cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent steward = addReadySteward(player1);
        steward.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Liliana's Steward");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during its controller's upkeep")
    void cannotActivateOutsideMainPhase() {
        addReadySteward(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
        harness.assertOnBattlefield(player1, "Liliana's Steward");
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        addReadySteward(player1);
        addReadySteward(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate during the postcombat main phase and discard a noncreature card")
    void canActivateDuringPostcombatMainPhase() {
        addReadySteward(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new WalkingCorpse(), new Duress()));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Duress");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadySteward(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LilianasSteward());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
