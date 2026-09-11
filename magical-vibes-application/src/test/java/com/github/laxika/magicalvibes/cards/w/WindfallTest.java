package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Windfall.class, Abundance.class, Island.class, Plains.class})
class WindfallTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws equal to the greatest hand discarded")
    void everyoneDrawsGreatestDiscarded() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.setHand(player1, List.of(new Windfall(), new Island()));
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Island);
        assertThat(gd.playerHands.get(player2.getId())).allMatch(card -> card instanceof Plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With empty hands, Windfall discards and draws nothing")
    void emptyHandsDoNothing() {
        harness.setHand(player1, List.of(new Windfall()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Waits for the active player's draw replacement before the opponent draws")
    void waitsForActivePlayersDrawReplacementBeforeOpponentDraws() {
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, List.of(new Island(), new Plains(), new Island()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.setHand(player1, List.of(new Windfall(), new Island()));
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
}
