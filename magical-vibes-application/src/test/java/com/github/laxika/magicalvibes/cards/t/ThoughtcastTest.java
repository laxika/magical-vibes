package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThoughtcastTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts lets Thoughtcast be cast for only blue mana")
    void affinityReducesGenericCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new Thoughtcast()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

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
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new Thoughtcast()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
