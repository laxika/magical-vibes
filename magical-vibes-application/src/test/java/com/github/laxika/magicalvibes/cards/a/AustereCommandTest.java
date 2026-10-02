package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.b.BoggartForager;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AustereCommand.class, SpringleafDrum.class, BoggartShenanigans.class,
        BoggartForager.class, BlindSpotGiant.class, HillcomberGiant.class})
class AustereCommandTest extends BaseCardTest {

    @Nested
    @DisplayName("Artifacts and enchantments modes")
    class ArtifactsAndEnchantments {

        @Test
        @DisplayName("Choosing artifacts and enchantments destroys both types")
        void destroysArtifactsAndEnchantments() {
            harness.addToBattlefield(player1, new SpringleafDrum());
            harness.addToBattlefield(player2, new BoggartShenanigans());
            harness.addToBattlefield(player2, new HillcomberGiant());
            harness.setHand(player1, List.of(new AustereCommand()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorceryWithModes(player1, 0, 2, 0, 1);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Springleaf Drum");
            harness.assertNotOnBattlefield(player2, "Boggart Shenanigans");
            harness.assertOnBattlefield(player2, "Hillcomber Giant");
        }
    }

    @Nested
    @DisplayName("Creature mana value modes")
    class CreatureManaValueModes {

        @Test
        @DisplayName("Choosing low and high mana value modes destroys matching creatures only")
        void destroysCreaturesByManaValue() {
            harness.addToBattlefield(player1, new BlindSpotGiant());
            harness.addToBattlefield(player2, new HillcomberGiant());
            harness.addToBattlefield(player2, new SpringleafDrum());
            harness.setHand(player1, List.of(new AustereCommand()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorceryWithModes(player1, 0, 2, 2, 3);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
            harness.assertNotOnBattlefield(player2, "Hillcomber Giant");
            harness.assertOnBattlefield(player2, "Springleaf Drum");
        }
    }

    @Test
    @DisplayName("Choosing non-adjacent modes applies only those modes")
    void choosesNonAdjacentModes() {
        harness.addToBattlefield(player1, new SpringleafDrum());
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player2, new BlindSpotGiant());
        harness.addToBattlefield(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new AustereCommand()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorceryWithModes(player1, 0, 2, 0, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Springleaf Drum");
        harness.assertOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertNotOnBattlefield(player2, "Blind-Spot Giant");
        harness.assertOnBattlefield(player2, "Hillcomber Giant");
    }

    @Test
    @DisplayName("Chosen modes resolve in card order, not selection order")
    void resolvesModesInCardOrder() {
        harness.addToBattlefield(player2, new BoggartShenanigans());
        harness.addToBattlefield(player2, new BoggartForager());
        harness.setHand(player1, List.of(new AustereCommand()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorceryWithModes(player1, 0, 2, 2, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boggart Shenanigans");
        harness.assertNotOnBattlefield(player2, "Boggart Forager");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
