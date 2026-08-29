package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspiringStatuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Nonartifact spells can use improvise")
    void grantsImproviseToNonartifactSpells() {
        harness.addToBattlefield(player1, new InspiringStatuary());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Artifact spells do not get improvise")
    void doesNotGrantImproviseToArtifactSpells() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        harness.setHand(player1, List.of(new WurmsTooth()));

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(statuary.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(statuary.isTapped()).isFalse();
    }
}
