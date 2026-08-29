package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MycosynthGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces Mycosynth Golem's generic cost")
    void affinityReducesOwnCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new MycosynthGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact creature spells you cast have affinity for artifacts")
    void grantsAffinityToArtifactCreatureSpells() {
        harness.addToBattlefield(player1, new MycosynthGolem());
        harness.setHand(player1, List.of(new DarksteelMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("The granted affinity does not reduce non-artifact creature spells")
    void doesNotGrantAffinityToNonArtifactCreatures() {
        harness.addToBattlefield(player1, new MycosynthGolem());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
