package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MeditationPuzzle;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChiefEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells can be cast using convoke")
    void grantsConvokeToArtifactSpells() {
        harness.addToBattlefield(player1, new ChiefEngineer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        UUID convokeCreatureId = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokeCreatureId));

        Permanent convokeCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(convokeCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Convoke is not granted to nonartifact spells")
    void doesNotGrantConvokeToNonartifactSpells() {
        harness.addToBattlefield(player1, new ChiefEngineer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeditationPuzzle()));

        UUID convokeCreatureId = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convokeCreatureId)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();
    }
}
