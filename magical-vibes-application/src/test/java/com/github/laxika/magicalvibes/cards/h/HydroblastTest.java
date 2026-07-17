package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HydroblastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target spell if it's red")
    class CounterMode {

        @Test
        @DisplayName("Counters a red spell")
        void countersRedSpell() {
            HillGiant giant = new HillGiant();
            harness.setHand(player1, List.of(giant));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.setHand(player2, List.of(new Hydroblast()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, giant.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Hill Giant"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Hill Giant"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        }

        @Test
        @DisplayName("Does nothing to a non-red spell (it resolves)")
        void doesNothingToNonRedSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Hydroblast()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, bears.getId());
            harness.passBothPriorities(); // Hydroblast resolves, does nothing
            harness.passBothPriorities(); // Grizzly Bears resolves onto the battlefield

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Hydroblast"));
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target permanent if it's red")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a red permanent")
        void destroysRedPermanent() {
            harness.addToBattlefield(player2, new HillGiant());

            harness.setHand(player1, List.of(new Hydroblast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Hill Giant"));
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Hill Giant"));
        }

        @Test
        @DisplayName("Does nothing to a non-red permanent")
        void doesNothingToNonRedPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new Hydroblast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Hydroblast"));
        }
    }
}
