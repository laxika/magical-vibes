package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thoughtcast.class, AlphaMyr.class, LeoninSkyhunter.class})
class ThoughtcastTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts lets Thoughtcast be cast for only blue mana")
    void affinityReducesGenericCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new AlphaMyr());
        }
        harness.setLibrary(player1, List.of(new AlphaMyr(), new AlphaMyr()));

        harness.castFromHand(player1, new Thoughtcast(), "{U}");

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gameData.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Thoughtcast");
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new AlphaMyr());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new Thoughtcast(), "{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity reduces the generic cost by one for each artifact")
    void affinityReducesGenericCostOnePerArtifact() {
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new AlphaMyr());
        }

        harness.castFromHand(player1, new Thoughtcast(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity does not count nonartifact permanents")
    void affinityDoesNotCountNonartifactPermanents() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new LeoninSkyhunter());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new Thoughtcast(), "{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
