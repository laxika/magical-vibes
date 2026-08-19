package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GleefulDemolitionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's artifact without creating tokens")
    void destroysOpponentsArtifactWithoutTokens() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");

        cast(targetId);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
    }

    @Test
    @DisplayName("Destroys your artifact and creates three Phyrexian Goblin tokens")
    void destroysOwnArtifactAndCreatesTokens() {
        harness.addToBattlefield(player1, new Spellbook());
        UUID targetId = harness.getPermanentId(player1, "Spellbook");

        cast(targetId);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(3);
        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(
                            CardSubtype.PHYREXIAN, CardSubtype.GOBLIN);
                });
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GleefulDemolition()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void cast(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GleefulDemolition()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
