package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoomedDissenterTest extends BaseCardTest {

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Doomed Dissenter dies, a 2/2 black Zombie token is created")
        void deathTriggerCreatesZombieToken() {
            harness.addToBattlefield(player1, new DoomedDissenter());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Doomed Dissenter dies

            GameData gd = harness.getGameData();

            harness.assertInGraveyard(player1, "Doomed Dissenter");

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            List<Permanent> tokens = findPermanents(player1, "Zombie");
            assertThat(tokens).hasSize(1);

            Permanent zombie = tokens.getFirst();
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().isToken()).isTrue();
        }

        @Test
        @DisplayName("Doomed Dissenter death trigger belongs to its controller")
        void deathTriggerBelongsToController() {
            harness.addToBattlefield(player2, new DoomedDissenter());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Doomed Dissenter dies
            harness.passBothPriorities(); // Resolve death trigger

            List<Permanent> player2Tokens = findPermanents(player2, "Zombie");
            assertThat(player2Tokens).hasSize(1);

            List<Permanent> player1Tokens = findPermanents(player1, "Zombie");
            assertThat(player1Tokens).isEmpty();
        }
    }
}
