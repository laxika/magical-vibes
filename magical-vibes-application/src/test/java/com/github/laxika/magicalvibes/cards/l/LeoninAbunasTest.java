package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninAbunas.class, AetherSpellbomb.class, AlphaMyr.class, Atog.class, Shatter.class})
class LeoninAbunasTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts and artifact creatures you control have hexproof")
    void grantsHexproofToOwnArtifacts() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not grant hexproof to nonartifacts or opponents' artifacts")
    void limitsHexproofToOwnArtifacts() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AetherSpellbomb());

        assertThat(gqs.hasKeyword(gd, nonartifact, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target an artifact you control")
    void opponentCannotTargetOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact controller can target their own artifact")
    void controllerCanTargetOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
