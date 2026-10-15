package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverSerpent.class, Island.class})
class RiverSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("River Serpent can attack with five cards in its controller's graveyard")
    void canAttackWithFiveCardsInGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, fiveCards());

        addCreatureReady(player1, new RiverSerpent());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("River Serpent cannot attack with fewer than five cards in its controller's graveyard")
    void cannotAttackWithFourCardsInGraveyard() {
        List<Card> four = fiveCards();
        four.remove(0);
        harness.setGraveyard(player1, four);

        addCreatureReady(player1, new RiverSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller's own graveyard counts toward the five")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, fiveCards());

        addCreatureReady(player1, new RiverSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling {U} discards River Serpent and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RiverSerpent()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "River Serpent");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("River Serpent can block with an empty graveyard")
    void canBlockWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, fiveCards());
        addCreatureReady(player2, new RiverSerpent());
        Permanent blocker = addCreatureReady(player1, new RiverSerpent());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "River Serpent");
        harness.assertInGraveyard(player2, "River Serpent");
    }

    @Test
    @DisplayName("Cycling pays mana and discards before the draw resolves")
    void cyclingPaysCostsBeforeResolution() {
        harness.setHand(player1, List.of(new RiverSerpent()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "River Serpent");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with colorless mana")
    void cyclingRequiresBlueMana() {
        harness.setHand(player1, List.of(new RiverSerpent()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "River Serpent");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(new Island());
        }
        return cards;
    }

}
