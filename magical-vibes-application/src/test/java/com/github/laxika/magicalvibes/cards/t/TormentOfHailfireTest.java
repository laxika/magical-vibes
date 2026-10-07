package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnquenchableThirst;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormentOfHailfire.class, Forest.class, GrizzlyBears.class, UnquenchableThirst.class, TajuruPreserver.class})
class TormentOfHailfireTest extends BaseCardTest {

    private static final String LOSE_LIFE = "Lose 3 life";

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
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

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
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

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
        harness.assertOnBattlefield(player2, "Grizzly Bears");
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

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bearsId);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
        harness.assertInGraveyard(player2, "Forest");
    }

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
        harness.assertInGraveyard(player1, "Torment of Hailfire");
    }

    @Test
    @DisplayName("Each iteration can use a different penalty and exhausted alternatives cause life loss")
    void mixedPenaltiesThenLifeLoss() {
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, creatureId);
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Torment of Hailfire");
    }

    @Test
    @DisplayName("An Aura whose creature was sacrificed remains available in the next iteration")
    void canSacrificeUnattachedAuraOnNextIteration() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var aura = harness.addToBattlefieldAndReturn(player2, new UnquenchableThirst());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, creature.getId());
        harness.assertOnBattlefield(player2, "Unquenchable Thirst");
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, aura.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Unquenchable Thirst");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("All opponents choose before any penalty is applied in a multiplayer iteration")
    void multiplayerPenaltiesWaitUntilAllOpponentsChoose() {
        Player player3 = new Player(UUID.randomUUID(), "Charlie");
        UUID id = player3.getId();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        harness.setLife(player3, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player3, List.of(new Forest()));
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleListChoice(player2, LOSE_LIFE);

        harness.assertLife(player2, 20);
        harness.assertLife(player3, 20);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player3, LOSE_LIFE);
        harness.assertLife(player2, 17);
        harness.assertLife(player3, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent protected from sacrifice must discard or lose life")
    void sacrificeProhibitionLeavesOnlyLifeLossWhenHandIsEmpty() {
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TormentOfHailfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Torment of Hailfire");
    }
}
