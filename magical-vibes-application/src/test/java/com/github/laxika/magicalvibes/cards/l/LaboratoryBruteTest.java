package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaboratoryBrute.class, Forest.class})
class LaboratoryBruteTest extends BaseCardTest {

    @Test
    void enteringBattlefieldMillsFourCardsFromControllerLibrary() {
        List<Forest> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(new LaboratoryBrute()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, cards);

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardBefore + 4);
    }

    @Test
    void enteringBattlefieldMillsOnlyAvailableCardsAndNotOpponentLibrary() {
        harness.setHand(player1, List.of(new LaboratoryBrute()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new Forest()));

        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(opponentGraveyardBefore);
    }

    @Test
    void millsOnlyTheTopFourCardsAfterTheEnterTriggerResolves() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, remaining));
        harness.setHand(player1, List.of(new LaboratoryBrute()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Laboratory Brute");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(remaining);
    }

    @Test
    void enteringWithAnEmptyLibraryDoesNotLoseTheGame() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LaboratoryBrute()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Laboratory Brute");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
