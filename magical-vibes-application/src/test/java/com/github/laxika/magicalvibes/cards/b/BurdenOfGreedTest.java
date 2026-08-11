package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurdenOfGreedTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses one life for each tapped artifact they control")
    void losesLifeForEachTappedArtifactOfTargetPlayer() {
        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        tappedArtifact.tap();
        Permanent secondTappedArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        secondTappedArtifact.tap();
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        opponentArtifact.tap();

        castBurdenOfGreed(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Target player with no tapped artifacts loses no life")
    void noTappedArtifactsCausesNoLifeLoss() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBurdenOfGreed(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Burden of Greed can target only a player")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new BurdenOfGreed()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Spellbook")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    private void castBurdenOfGreed(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BurdenOfGreed()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
