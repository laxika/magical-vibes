package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IndrikStomphowler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BiomanticMastery.class, BreedingPool.class, IndrikStomphowler.class})
class BiomanticMasteryTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new BiomanticMastery()));
        harness.setLibrary(player1, List.of(
                new BreedingPool(), new BreedingPool(), new BreedingPool(), new BreedingPool(),
                new BreedingPool()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Draws for the creatures controlled by each targeted player")
    void drawsForEachTargetedPlayersCreatures() {
        prepare();
        harness.addToBattlefield(player1, new IndrikStomphowler());
        harness.addToBattlefield(player1, new IndrikStomphowler());
        harness.addToBattlefield(player2, new IndrikStomphowler());

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
    }

    @Test
    @DisplayName("Counts only creatures, so a target with none contributes no cards")
    void ignoresNoncreaturesAndDrawsNothingForTargetWithNoCreatures() {
        prepare();
        harness.addToBattlefield(player1, new IndrikStomphowler());
        harness.addToBattlefield(player2, new BreedingPool());

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    @DisplayName("The two target players must be different")
    void cannotTargetTheSamePlayerTwice() {
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
