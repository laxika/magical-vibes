package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomedTraveler.class, WrathOfGod.class})
class DoomedTravelerTest extends BaseCardTest {

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({DoomedTraveler.class, WrathOfGod.class})
    class DeathTriggerTests {

        @Test
        @DisplayName("When Doomed Traveler dies, a 1/1 white Spirit token with flying is created")
        void deathTriggerCreatesSpiritToken() {
            harness.addToBattlefield(player1, new DoomedTraveler());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Doomed Traveler dies

            GameData gd = harness.getGameData();

            // Doomed Traveler should be in the graveyard
            harness.assertInGraveyard(player1, "Doomed Traveler");

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            // A 1/1 Spirit token with flying should be on the battlefield
            List<Permanent> tokens = findPermanents(player1, "Spirit");
            assertThat(tokens).hasSize(1);

            Permanent spiritToken = tokens.getFirst();
            assertThat(spiritToken.getCard().getPower()).isEqualTo(1);
            assertThat(spiritToken.getCard().getToughness()).isEqualTo(1);
            assertThat(spiritToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spiritToken.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spiritToken.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(spiritToken.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(spiritToken.getCard().isToken()).isTrue();
        }

        @Test
        @DisplayName("Doomed Traveler death trigger belongs to its controller")
        void deathTriggerBelongsToController() {
            harness.addToBattlefield(player2, new DoomedTraveler());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Doomed Traveler dies
            harness.passBothPriorities(); // Resolve death trigger

            // The Spirit token should be on player2's battlefield (the Doomed Traveler's controller)
            List<Permanent> player2Tokens = findPermanents(player2, "Spirit");
            assertThat(player2Tokens).hasSize(1);

            // Player1 should have no Spirit tokens
            List<Permanent> player1Tokens = findPermanents(player1, "Spirit");
            assertThat(player1Tokens).isEmpty();
        }

        @Test
        @DisplayName("Each Traveler dying simultaneously creates its own Spirit after the board wipe")
        void simultaneousDeathsCreateSeparateTokens() {
            harness.addToBattlefield(player1, new DoomedTraveler());
            harness.addToBattlefield(player1, new DoomedTraveler());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities();

            assertThat(findPermanents(player1, "Doomed Traveler")).isEmpty();
            assertThat(findPermanents(player1, "Spirit")).isEmpty();
            assertThat(harness.getGameData().stack).hasSize(2);

            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Spirit")).hasSize(1);
            assertThat(harness.getGameData().stack).hasSize(1);

            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Spirit")).hasSize(2);
            assertThat(harness.getGameData().stack).isEmpty();
            assertThat(findPermanents(player2, "Spirit")).isEmpty();
        }
    }
}
