package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({IslandSanctuary.class, GrizzlyBears.class, HowlingMine.class, RiverBoa.class, SuntailHawk.class})
class IslandSanctuaryTest extends BaseCardTest {

    /** Advance the controller to their draw step and answer the "skip your draw?" prompt. */
    private void resolveDrawStepChoice(Player controller, boolean skip) {
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        advanceToUpkeep(controller);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UPKEEP -> DRAW: Island Sanctuary offers the skip may-ability
        harness.handleMayAbilityChosen(controller, skip);
    }
    @Test
    @DisplayName("Skipping the draw does not draw a card")
    void skippingDoesNotDraw() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of());

        resolveDrawStepChoice(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can use a second Island Sanctuary if the first replacement is declined")
    void canUseSecondSanctuaryAfterDecliningFirst() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveDrawStepChoice(player1, false);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can use Island Sanctuary for an extra draw during the draw step")
    void canUseSanctuaryForExtraDrawDuringDrawStep() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveDrawStepChoice(player1, true);
        if (!gd.interaction.isAwaitingInput()) {
            resolveAllTriggers();
        }

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining draws a card as normal")
    void decliningDrawsNormally() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of());

        resolveDrawStepChoice(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("After skipping, a non-flying, non-islandwalk creature can't attack the controller")
    void nonFlyerCantAttackAfterSkip() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, true);

        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("After skipping, a flying creature can still attack the controller")
    void flyerCanAttackAfterSkip() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, true);

        addCreatureReady(player2, new SuntailHawk());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("After skipping, an islandwalk creature can still attack the controller")
    void islandwalkerCanAttackAfterSkip() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, true);

        addCreatureReady(player2, new RiverBoa());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Declining leaves no shield: a ground creature can attack the controller")
    void noShieldWhenDeclined() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, false);

        addCreatureReady(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The shield wears off at the start of the controller's next turn")
    void shieldExpiresAtControllerNextTurn() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, true);

        // "Until your next turn" — the floating shield expires when player1's turn begins again.
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        addCreatureReady(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The shield remains after Island Sanctuary leaves the battlefield")
    void shieldPersistsAfterSanctuaryLeaves() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, true);

        Permanent sanctuary = findPermanent(player1, "Island Sanctuary");
        gd.playerBattlefields.get(player1.getId()).remove(sanctuary);
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
