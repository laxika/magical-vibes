package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlinkmothInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Untaps all artifacts but no non-artifact permanents")
    void untapsAllArtifactsButNotNonArtifacts() {
        List<Permanent> playerArtifacts = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            playerArtifacts.add(harness.addToBattlefieldAndReturn(player1, new Spellbook()));
        }
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        playerArtifacts.forEach(Permanent::tap);
        opponentArtifact.tap();
        nonArtifact.tap();

        harness.setHand(player1, List.of(new BlinkmothInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(playerArtifacts).allMatch(permanent -> !permanent.isTapped());
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(nonArtifact.isTapped()).isTrue();
    }
}
