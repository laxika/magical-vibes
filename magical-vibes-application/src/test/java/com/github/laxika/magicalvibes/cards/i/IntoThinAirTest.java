package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoThinAir.class, ConjurersBauble.class, MyrServitor.class, SparkElemental.class})
class IntoThinAirTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.addToBattlefield(player2, new ConjurersBauble());
        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Conjurer's Bauble"));

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Returns the target artifact to its owner's hand")
    void returnsTargetArtifactToItsOwnersHand() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.addToBattlefield(player2, new ConjurersBauble());
        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Conjurer's Bauble"));

        harness.assertNotOnBattlefield(player2, "Conjurer's Bauble");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Conjurer's Bauble");
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new ConjurersBauble());
        }
        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Conjurer's Bauble")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts artifact creatures as artifacts")
    void affinityCountsArtifactCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.addToBattlefield(player1, new MyrServitor());
        harness.addToBattlefield(player2, new ConjurersBauble());
        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Conjurer's Bauble"));

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
        }
        harness.addToBattlefield(player2, new SparkElemental());
        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Spark Elemental")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
