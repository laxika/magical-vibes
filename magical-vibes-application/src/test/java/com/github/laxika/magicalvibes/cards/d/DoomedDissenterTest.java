package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FinalReward;
import com.github.laxika.magicalvibes.cards.s.SplendidAgony;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomedDissenter.class, WrathOfGod.class, SplendidAgony.class, FinalReward.class})
class DoomedDissenterTest extends BaseCardTest {

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({DoomedDissenter.class, WrathOfGod.class, SplendidAgony.class, FinalReward.class})
    class DeathTriggerTests {

        @Test
        @DisplayName("When Doomed Dissenter dies, a 2/2 black Zombie token is created")
        void deathTriggerCreatesZombieToken() {
            harness.addToBattlefield(player1, new DoomedDissenter());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

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

            harness.castAndResolveSorcery(player1, 0, 0);
            harness.passBothPriorities(); // Resolve death trigger

            List<Permanent> player2Tokens = findPermanents(player2, "Zombie");
            assertThat(player2Tokens).hasSize(1);

            List<Permanent> player1Tokens = findPermanents(player1, "Zombie");
            assertThat(player1Tokens).isEmpty();
        }

        @Test
        @DisplayName("Each Dissenter dying simultaneously creates its own Zombie")
        void simultaneousDeathsCreateSeparateTokens() {
            Permanent first = harness.addToBattlefieldAndReturn(player1, new DoomedDissenter());
            Permanent second = harness.addToBattlefieldAndReturn(player1, new DoomedDissenter());
            harness.setHand(player1, List.of(new SplendidAgony()));
            harness.addMana(player1, ManaColor.BLACK, 3);

            harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

            harness.assertNotOnBattlefield(player1, "Doomed Dissenter");
            assertThat(gd.stack).hasSize(2);
            assertThat(findPermanents(player1, "Zombie")).isEmpty();

            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Zombie")).hasSize(1);
            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Zombie")).hasSize(2);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Exiling Doomed Dissenter does not trigger its death ability")
        void exileDoesNotCreateToken() {
            Permanent dissenter = harness.addToBattlefieldAndReturn(player2, new DoomedDissenter());
            harness.setHand(player1, List.of(new FinalReward()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castAndResolveInstant(player1, 0, dissenter.getId());

            harness.assertNotOnBattlefield(player2, "Doomed Dissenter");
            assertThat(gd.getPlayerExiledCards(player2.getId())).contains(dissenter.getCard());
            assertThat(gd.stack).isEmpty();
            assertThat(findPermanents(player1, "Zombie")).isEmpty();
            assertThat(findPermanents(player2, "Zombie")).isEmpty();
        }
    }
}
