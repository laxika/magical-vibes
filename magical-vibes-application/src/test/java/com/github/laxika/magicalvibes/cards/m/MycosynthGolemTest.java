package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BatteredGolem;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.s.SylvokExplorer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MycosynthGolem.class, ConjurersBauble.class, BatteredGolem.class, SylvokExplorer.class})
class MycosynthGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces Mycosynth Golem's generic cost")
    void affinityReducesOwnCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ConjurersBauble());
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
        harness.setHand(player1, List.of(new BatteredGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("The granted affinity does not reduce non-artifact creature spells")
    void doesNotGrantAffinityToNonArtifactCreatures() {
        harness.addToBattlefield(player1, new MycosynthGolem());
        harness.setHand(player1, List.of(new SylvokExplorer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new ConjurersBauble());
        }
        harness.setHand(player1, List.of(new MycosynthGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted affinity does not reduce noncreature artifact spells")
    void doesNotGrantAffinityToNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new MycosynthGolem());
        harness.setHand(player1, List.of(new ConjurersBauble()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
