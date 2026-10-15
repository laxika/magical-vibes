package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.l.LordOfAtlantis;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.m.MesaPegasus;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({IslandSanctuary.class, GrizzlyBears.class, HowlingMine.class, JayemdaeTome.class,
        LordOfAtlantis.class, MerfolkOfThePearlTrident.class, MesaPegasus.class})
class IslandSanctuaryTest extends BaseCardTest {

    /** Advance the controller to their draw step and answer the "skip your draw?" prompt. */
    private void resolveDrawStepChoice(Player controller, boolean skip) {
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        advanceToUpkeep(controller);
        harness.passUntil(controller, TurnStep.DRAW);
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

        addCreatureReady(player2, new MesaPegasus());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("After skipping, an islandwalk creature can still attack the controller")
    void islandwalkerCanAttackAfterSkip() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        resolveDrawStepChoice(player1, true);

        addCreatureReady(player2, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new LordOfAtlantis());

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

    @Test
    @DisplayName("Skipping a draw from an empty library does not lose the game")
    void canSkipDrawFromEmptyLibrary() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.withAutoStop(TurnStep.DRAW, () -> resolveDrawStepChoice(player1, true));

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Advancing through the next turn removes Sanctuary protection")
    void shieldExpiresThroughNormalTurnProgression() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.withAutoStop(TurnStep.DRAW, () -> resolveDrawStepChoice(player1, true));
        addCreatureReady(player2, new GrizzlyBears());

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A skipped draw does not force the controller to skip later draws")
    void canDeclineReplacementForExtraDrawAfterSkippingNormalDraw() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.withAutoStop(TurnStep.DRAW, () -> {
            resolveDrawStepChoice(player1, true);
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The controller can draw normally then skip an activated draw in their draw step")
    void canSkipActivatedDrawAfterDrawingNormally() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        GrizzlyBears skippedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard, skippedCard));

        harness.withAutoStop(TurnStep.DRAW, () -> {
            resolveDrawStepChoice(player1, false);
            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1, 1, null, null);
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skippedCard);
        addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Sanctuary cannot replace its controller's draw in an opponent's draw step")
    void cannotSkipDrawDuringOpponentsDrawStep() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passUntil(player2, TurnStep.DRAW);

        harness.withAutoStop(TurnStep.DRAW, () -> {
            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1, 1, null, null);
            resolveAllTriggers();
        });

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        addCreatureReady(player2, new GrizzlyBears());
        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Sanctuary cannot replace draws outside the draw step")
    void cannotSkipDrawDuringMainPhase() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1, 1, null, null);
            resolveAllTriggers();
        });

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        addCreatureReady(player2, new GrizzlyBears());
        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }
}
