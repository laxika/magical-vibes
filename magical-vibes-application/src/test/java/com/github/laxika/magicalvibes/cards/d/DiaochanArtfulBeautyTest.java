package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiaochanArtfulBeautyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted creature, then an opponent chooses a creature to destroy")
    void destroysTargetThenOpponentChooses() {
        setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // First destruction (controller's choice) already happened; opponent is now prompted.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DestroyChosenCreature.class);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));

        // Opponent picks one of the controller's creatures to destroy.
        harness.handlePermanentChosen(player2, ownBears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Diaochan, Artful Beauty"));
    }

    @Test
    @DisplayName("Second destruction auto-resolves when only one creature remains")
    void secondDestructionAutoResolvesWithOneCreature() {
        setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Only Diaochan remains after the first destruction, so it is destroyed automatically
        // (the opponent's forced choice with a single legal creature).
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Diaochan, Artful Beauty"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Diaochan, Artful Beauty"));
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupDiaochanOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupDiaochanOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new DiaochanArtfulBeauty());
        findPermanent(player1, "Diaochan, Artful Beauty").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
