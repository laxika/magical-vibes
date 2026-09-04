package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlueElementalBlast.class, Fireball.class, GrizzlyBears.class, HillGiant.class})
class BlueElementalBlastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target red spell")
    class CounterRedSpellMode {

        @Test
        @DisplayName("Counters a red spell")
        void countersRedSpell() {
            Fireball fireball = new Fireball();
            harness.setHand(player2, List.of(fireball));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.forceActivePlayer(player2);
            harness.castSorcery(player2, 0, 0, List.of(player1.getId()));
            harness.passPriority(player2);

            harness.castInstant(player1, 0, 0, fireball.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player2, "Fireball");
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
            var hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, hillGiant.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Hill Giant");
            harness.assertInGraveyard(player2, "Hill Giant");
        }

        @Test
        @DisplayName("Cannot destroy a non-red permanent")
        void cannotDestroyNonRedPermanent() {
            var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new BlueElementalBlast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
