package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlueElementalBlastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target red spell")
    class CounterRedSpellMode {

        @Test
        @DisplayName("Counters a red spell")
        void countersRedSpell() {
            Shock shock = new Shock();
            harness.setHand(player2, List.of(shock));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.forceActivePlayer(player2);
            harness.castInstant(player2, 0, player1.getId());
            harness.passPriority(player2);

            harness.castInstant(player1, 0, 0, shock.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Shock"));
        }

        @Test
        @DisplayName("Cannot counter a non-red spell")
        void cannotCounterNonRedSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player2, List.of(bears));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target red permanent")
    class DestroyRedPermanentMode {

        @Test
        @DisplayName("Destroys a red permanent")
        void destroysRedPermanent() {
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Hill Giant");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Hill Giant"));
        }

        @Test
        @DisplayName("Cannot destroy a non-red permanent")
        void cannotDestroyNonRedPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
