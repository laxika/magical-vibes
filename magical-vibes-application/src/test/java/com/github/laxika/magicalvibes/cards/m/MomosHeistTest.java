package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({MomosHeist.class, Ornithopter.class, GrizzlyBears.class})
class MomosHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of, untaps, and grants haste to the target artifact")
    void resolvesArtifactHeist() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        artifact.tap();

        cast(artifact);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Sacrifices the artifact at the beginning of the next end step")
    void sacrificesArtifactAtNextEndStep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(artifact);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(artifact.getId()));
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Only targets artifacts")
    void requiresArtifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MomosHeist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void cast(Permanent artifact) {
        harness.setHand(player1, List.of(new MomosHeist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, artifact.getId());
    }
}
