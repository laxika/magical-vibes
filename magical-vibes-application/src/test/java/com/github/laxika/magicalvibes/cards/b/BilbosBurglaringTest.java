package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilbosBurglaring.class, MindStone.class, GrizzlyBears.class})
class BilbosBurglaringTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of one artifact controlled by the opponent")
    void gainsPermanentControlOfTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castBilbosBurglaring(List.of(artifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("May choose no artifacts")
    void canChooseNoTargets() {
        castBilbosBurglaring(List.of());

        harness.assertInGraveyard(player1, "Bilbo's Burglaring");
    }

    @Test
    @DisplayName("Cannot choose two artifacts controlled by the same opponent")
    void cannotChooseTwoArtifactsOfSameOpponent() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Cannot target an artifact you control")
    void cannotTargetOwnArtifact() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void castBilbosBurglaring(List<UUID> targetIds) {
        prepareCast();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new BilbosBurglaring()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
