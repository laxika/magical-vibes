package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Broodstar.class, LeoninSkyhunter.class, Ornithopter.class})
class BroodstarTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of artifacts you control")
    void powerAndToughnessEqualControlledArtifacts() {
        Permanent broodstar = harness.addToBattlefieldAndReturn(player1, new Broodstar());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new LeoninSkyhunter());

        assertThat(gqs.getEffectivePower(gd, broodstar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, broodstar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness update as controlled artifacts enter and leave")
    void powerAndToughnessUpdateAsArtifactsChange() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent broodstar = harness.addToBattlefieldAndReturn(player1, new Broodstar());

        assertThat(gqs.getEffectivePower(gd, broodstar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, broodstar)).isEqualTo(1);

        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, broodstar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, broodstar)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        assertThat(gqs.getEffectivePower(gd, broodstar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, broodstar)).isEqualTo(1);
    }

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.setHand(player1, List.of(new Broodstar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player2, new Ornithopter());
        }
        harness.setHand(player1, List.of(new Broodstar()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity does not reduce Broodstar's colored mana cost")
    void affinityDoesNotReduceColoredManaCost() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.setHand(player1, List.of(new Broodstar()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Broodstar enters as a 0/0 and dies when you control no artifacts")
    void entersAsZeroZeroWithoutArtifacts() {
        harness.setHand(player1, List.of(new Broodstar()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Broodstar");
        harness.assertInGraveyard(player1, "Broodstar");
    }
}
