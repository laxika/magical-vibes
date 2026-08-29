package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CuratorOfDestiniesTest extends BaseCardTest {

    private void castCuratorAndReachFaceUpPileChoice(Card... library) {
        harness.setLibrary(player1, List.of(library));
        harness.setHand(player1, List.of(new CuratorOfDestinies()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The spell cannot be countered")
    void spellCannotBeCountered() {
        CuratorOfDestinies curator = new CuratorOfDestinies();
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains(), new GrizzlyBears()));
        harness.setHand(player1, List.of(curator));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, curator.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Curator of Destinies");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The controller separates a face-up and face-down pile, and the opponent can choose the face-down pile")
    void opponentChoosesFaceDownPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        castCuratorAndReachFaceUpPileChoice(island, forest, swamp, plains, bears);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains("Island", "Forest", "3 cards")
                .doesNotContain("Swamp", "Plains", "Grizzly Bears");

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains, bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
    }

    @Test
    @DisplayName("Choosing the face-up pile puts it into hand and puts the face-down pile into the graveyard")
    void opponentChoosesFaceUpPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        castCuratorAndReachFaceUpPileChoice(island, forest, swamp, plains, bears);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains, bears);
    }
}
