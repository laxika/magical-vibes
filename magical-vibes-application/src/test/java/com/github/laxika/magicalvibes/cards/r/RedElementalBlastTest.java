package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedElementalBlastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target blue spell")
    class CounterBlueSpellMode {

        @Test
        @DisplayName("Counters a blue spell")
        void countersBlueSpell() {
            FugitiveWizard wizard = new FugitiveWizard();
            harness.setHand(player2, List.of(wizard));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            harness.castInstant(player1, 0, 0, wizard.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        }

        @Test
        @DisplayName("Cannot counter a non-blue spell")
        void cannotCounterNonBlueSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player2, List.of(bears));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target blue permanent")
    class DestroyBluePermanentMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            harness.addToBattlefield(player2, new FugitiveWizard());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
        }

        @Test
        @DisplayName("Cannot destroy a non-blue permanent")
        void cannotDestroyNonBluePermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
