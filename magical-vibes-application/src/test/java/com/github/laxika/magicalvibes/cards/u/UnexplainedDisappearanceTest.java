package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnexplainedDisappearanceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and surveils 1")
    void returnsTargetCreatureAndSurveils() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Leaves the top card on the library when surveil is declined")
    void declinesSurveil() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
