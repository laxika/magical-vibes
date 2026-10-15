package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrEnforcer.class, Ornithopter.class, LeoninSkyhunter.class})
class MyrEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.setHand(player1, List.of(new MyrEnforcer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity can reduce the generic mana cost to zero")
    void affinityCanReduceCostToZero() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.setHand(player1, List.of(new MyrEnforcer()));

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player2, new Ornithopter());
        }
        harness.setHand(player1, List.of(new MyrEnforcer()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity does not count nonartifact permanents")
    void affinityDoesNotCountNonartifactPermanents() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new LeoninSkyhunter());
        }
        harness.setHand(player1, List.of(new MyrEnforcer()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tapped artifacts still reduce the casting cost")
    void affinityCountsTappedArtifacts() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefieldAndReturn(player1, new Ornithopter()).tap();
        }
        harness.setHand(player1, List.of(new MyrEnforcer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifacts in hand and graveyard do not reduce the casting cost")
    void affinityDoesNotCountArtifactsOutsideBattlefield() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.setHand(player1, List.of(new MyrEnforcer(), new Ornithopter()));
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("More than seven artifacts allow a free cast without generating mana")
    void affinityCannotReduceCostBelowZero() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.setHand(player1, List.of(new MyrEnforcer()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without artifacts Myr Enforcer requires seven mana and resolves normally")
    void castsForFullCostWithoutArtifacts() {
        harness.setHand(player1, List.of(new MyrEnforcer()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Myr Enforcer");
        harness.assertNotInHand(player1, "Myr Enforcer");
        assertThat(gd.stack).isEmpty();
    }
}
