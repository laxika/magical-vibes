package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyroblastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target spell if it's blue")
    class CounterMode {

        @Test
        @DisplayName("Counters a blue spell")
        void countersBlueSpell() {
            FugitiveWizard wizard = new FugitiveWizard();
            harness.setHand(player1, List.of(wizard));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.setHand(player2, List.of(new Pyroblast()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, wizard.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Fugitive Wizard"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        }

        @Test
        @DisplayName("Does nothing to a non-blue spell (it resolves)")
        void doesNothingToNonBlueSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Pyroblast()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, bears.getId());
            harness.passBothPriorities(); // Pyroblast resolves, does nothing
            harness.passBothPriorities(); // Grizzly Bears resolves onto the battlefield

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Pyroblast"));
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target permanent if it's blue")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            harness.addToBattlefield(player2, new FugitiveWizard());

            harness.setHand(player1, List.of(new Pyroblast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Fugitive Wizard"));
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
        }

        @Test
        @DisplayName("Does nothing to a non-blue permanent")
        void doesNothingToNonBluePermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new Pyroblast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Pyroblast"));
        }
    }
}
