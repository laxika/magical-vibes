package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromancersAssistant.class, Forest.class})
class NecromancersAssistantTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NecromancersAssistant(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB mills three cards from the controller's library")
    void etbMillsThree() {
        harness.setLibrary(player1, List.of(
                new NecromancersAssistant(), new Forest(), new Forest(), new Forest(), new Forest()));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB does not mill the opponent")
    void etbDoesNotMillOpponent() {
        castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB mills only what is left when the library is smaller than three")
    void etbMillsRemainderOfSmallLibrary() {
        harness.setLibrary(player1, List.of(new Forest()));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB can resolve with an empty library")
    void etbWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Necromancer's Assistant");
    }

    @Test
    @DisplayName("Milling waits for the triggered ability to resolve and takes the top three cards")
    void millOccursOnTriggerResolution() {
        Forest first = new Forest();
        NecromancersAssistant second = new NecromancersAssistant();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NecromancersAssistant(), "{2}{B}");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third, fourth);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Necromancer's Assistant");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB mills its controller when the second player casts the creature")
    void etbMillsSecondPlayerController() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new NecromancersAssistant(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
