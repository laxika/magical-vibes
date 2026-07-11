package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AustereCommandTest extends BaseCardTest {

    @Nested
    @DisplayName("Artifacts and enchantments modes")
    class ArtifactsAndEnchantments {

        @Test
        @DisplayName("Choosing artifacts and enchantments destroys both types")
        void destroysArtifactsAndEnchantments() {
            harness.addToBattlefield(player1, new PithingNeedle());
            harness.addToBattlefield(player2, new Pacifism());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AustereCommand()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorceryWithModes(player1, 0, 2, 0, 1);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Pithing Needle"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Pacifism"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }

    @Nested
    @DisplayName("Creature mana value modes")
    class CreatureManaValueModes {

        @Test
        @DisplayName("Choosing low and high mana value modes destroys matching creatures only")
        void destroysCreaturesByManaValue() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new SerraAngel());
            harness.addToBattlefield(player2, new PithingNeedle());
            harness.setHand(player1, List.of(new AustereCommand()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorceryWithModes(player1, 0, 2, 2, 3);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Pithing Needle"));
        }
    }

    @Test
    @DisplayName("Chosen modes resolve in card order, not selection order")
    void resolvesModesInCardOrder() {
        harness.addToBattlefield(player2, new Pacifism());
        harness.addToBattlefield(player2, new PithingNeedle());
        harness.setHand(player1, List.of(new AustereCommand()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorceryWithModes(player1, 0, 2, 1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pithing Needle")
                        || p.getCard().getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("Choosing only one mode is rejected at cast time")
    void rejectsSingleModeSelection() {
        harness.setHand(player1, List.of(new AustereCommand()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode bitmask");
    }

    @Test
    @DisplayName("Choosing three modes is rejected at cast time")
    void rejectsThreeModeSelection() {
        harness.setHand(player1, List.of(new AustereCommand()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castSorceryWithModes(player1, 0, 2, 0, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
