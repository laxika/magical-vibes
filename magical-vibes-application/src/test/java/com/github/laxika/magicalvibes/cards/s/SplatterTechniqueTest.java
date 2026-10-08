package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RalZarekGuestLecturer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplatterTechnique.class, EnormousBaloth.class, GrizzlyBears.class, RalZarekGuestLecturer.class})
class SplatterTechniqueTest extends BaseCardTest {

    private void addManaFor(int extraRed) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2 + extraRed);
    }

    

    @Nested
    @DisplayName("Mode 0: Draw four cards")
    @CardUsed({SplatterTechnique.class, GrizzlyBears.class})
    class DrawMode {

        @Test
        @DisplayName("Controller draws four cards")
        void drawsFourCards() {
            harness.setHand(player1, List.of(new SplatterTechnique()));
            harness.setLibrary(player1, List.of(
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                    new GrizzlyBears(), new GrizzlyBears()));
            addManaFor(1);

            harness.castAndResolveSorcery(player1, 0, 0);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Mode 1: 4 damage to each creature and planeswalker")
    @CardUsed({SplatterTechnique.class, GrizzlyBears.class, EnormousBaloth.class, RalZarekGuestLecturer.class})
    class MassDamageMode {

        @Test
        @DisplayName("Kills creatures with toughness 4 or less on both sides")
        void killsSmallCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new SplatterTechnique()));
            addManaFor(1);

            harness.castAndResolveSorcery(player1, 0, 1);

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Does not kill creatures with toughness greater than 4")
        void doesNotKillLargeCreatures() {
            harness.addToBattlefield(player2, new EnormousBaloth());
            harness.setHand(player1, List.of(new SplatterTechnique()));
            addManaFor(1);

            harness.castAndResolveSorcery(player1, 0, 1);

            harness.assertOnBattlefield(player2, "Enormous Baloth");
        }

        @Test
        @CardUsed({SplatterTechnique.class, RalZarekGuestLecturer.class})
        void damagesPlaneswalkersWithoutDrawingOrDamagingPlayers() {
            var surviving = harness.addToBattlefieldAndReturn(player1, new RalZarekGuestLecturer());
            surviving.setCounterCount(CounterType.LOYALTY, 5);
            var dying = harness.addToBattlefieldAndReturn(player2, new RalZarekGuestLecturer());
            dying.setCounterCount(CounterType.LOYALTY, 4);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            harness.setLibrary(player1, List.of(new SplatterTechnique()));
            harness.setHand(player1, List.of(new SplatterTechnique()));
            addManaFor(1);

            harness.castAndResolveSorcery(player1, 0, 1);

            assertThat(surviving.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
            harness.assertOnBattlefield(player1, "Ral Zarek, Guest Lecturer");
            harness.assertNotOnBattlefield(player2, "Ral Zarek, Guest Lecturer");
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        }
    }
}
