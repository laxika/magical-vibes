package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TormentOfHailfireTest extends BaseCardTest {

    private static final String LOSE_LIFE = "Lose 3 life";

    // ===== Casting =====

    @Test
    @DisplayName("Casting stores the paid X on the stack entry")
    void castingStoresX() {
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=0 does nothing")
    void xZeroDoesNothing() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== No real choice: auto lose life =====

    @Test
    @DisplayName("An opponent with no hand and no nonland permanent just loses life, once per iteration")
    void losesLifeEachIterationWhenNoOtherOption() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest()); // a land is not a nonland permanent
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // 2 iterations x 3 life, no prompt needed, land untouched.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== Each choice branch =====

    @Test
    @DisplayName("Opponent may choose to lose life even with a permanent and a card")
    void opponentMayChooseToLoseLife() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player2, LOSE_LIFE);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent may sacrifice a nonland permanent instead of losing life")
    void opponentMaySacrificeNonlandPermanent() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        UUID bearsId = nonlandPermanentId(player2.getId(), "Grizzly Bears");
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bearsId);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent may discard a card instead of losing life")
    void opponentMayDiscardACard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Repetition =====

    @Test
    @DisplayName("The whole process repeats X times, prompting the opponent each iteration")
    void processRepeatsXTimes() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Iteration 1: discard.
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);
        // Iteration 2: discard again.
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== Illegal choice =====

    @Test
    @DisplayName("Choosing an option that isn't offered is rejected")
    void unofferedOptionRejected() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of()); // no cards: discard is not an option
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== Disposition =====

    @Test
    @DisplayName("Torment of Hailfire goes to its owner's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Torment of Hailfire"));
    }

    private UUID nonlandPermanentId(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId)
                .findFirst()
                .orElseThrow();
    }
}
