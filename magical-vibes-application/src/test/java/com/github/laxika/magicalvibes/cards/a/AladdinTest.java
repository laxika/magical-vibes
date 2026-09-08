package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AladdinTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gains control of target artifact")
    void gainsControlOfTargetArtifact() {
        Permanent aladdin = addReadyAladdin();
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(aladdin.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Stolen artifact returns when Aladdin leaves the battlefield")
    void stolenArtifactReturnsWhenAladdinLeaves() {
        Permanent aladdin = addReadyAladdin();
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, aladdin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.controlEffectsFor(artifact.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addReadyAladdin();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAladdin() {
        return addCreatureReady(player1, new Aladdin());
    }

    private Permanent addArtifact(Player player) {
        Permanent artifact = new Permanent(new MindStone());
        artifact.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(artifact);
        return artifact;
    }
}
