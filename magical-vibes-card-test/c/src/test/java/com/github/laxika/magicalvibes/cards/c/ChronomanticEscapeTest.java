package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.d.DustOfMoments;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChronomanticEscape.class, BlindPhantasm.class, DustOfMoments.class})
class ChronomanticEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("A resolved cast is exiled with three suspend time counters")
    void castExilesWithSuspendCounters() {
        ChronomanticEscape escape = castNormally();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(escape);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(escape.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("The attack restriction lasts through the opponent's turn and expires on the controller's next turn")
    void restrictionExpiresOnControllerNextTurn() {
        castNormally();

        gd.expireEndOfTurnFloatingEffects();
        addCreatureReady(player2, new BlindPhantasm());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The attack restriction still allows creatures to attack the other player")
    void restrictionOnlyProtectsItsController() {
        castNormally();
        addCreatureReady(player1, new BlindPhantasm());

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A suspended escape resolves for free and starts a new suspend countdown")
    void suspendRecastsForFree() {
        ChronomanticEscape escape = suspendCard();

        assertThat(gd.exiledCardTimeCounters).containsEntry(escape.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(escape.getId());
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(escape.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the escape exiled")
    void decliningSuspendCastLeavesCardExiled() {
        ChronomanticEscape escape = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        }
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(escape);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(escape.getId());
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    @Test
    @DisplayName("Removing two counters after resolution permits a recast at the next upkeep")
    void dustRemovesCountersFromResolvedEscape() {
        ChronomanticEscape escape = castNormally();
        castDustOfMoments(0);

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(escape);
        addCreatureReady(player2, new BlindPhantasm());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Adding two counters after resolution delays the recast until the fifth upkeep")
    void dustAddsCountersToResolvedEscape() {
        castNormally();
        castDustOfMoments(1);

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The recurring countdown advances only during its owner's upkeep")
    void resolvedEscapeRecastsOnlyAfterThreeOwnerUpkeeps() {
        ChronomanticEscape escape = castNormally();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player2);
            harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
            assertThat(gd.interaction.activeInteraction()).isNull();
            advanceToUpkeep(player1);
            harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
            if (i < 2) {
                assertThat(gd.interaction.activeInteraction()).isNull();
            }
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(escape);
        addCreatureReady(player2, new BlindPhantasm());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Removing the last counter creates a separate trigger before offering the free cast")
    void lastCounterCreatesRespondableCastTrigger() {
        ChronomanticEscape escape = suspendCard();
        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isSameAs(escape);
        });
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
    }

    private void castDustOfMoments(int mode) {
        harness.setHand(player1, List.of(new DustOfMoments()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }

    private ChronomanticEscape castNormally() {
        ChronomanticEscape escape = new ChronomanticEscape();
        harness.castFromHand(player1, escape, "{4}{W}{W}");
        harness.passBothPriorities();
        return escape;
    }

    private ChronomanticEscape suspendCard() {
        ChronomanticEscape escape = new ChronomanticEscape();
        harness.setHand(player1, List.of(escape));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        return escape;
    }
}
