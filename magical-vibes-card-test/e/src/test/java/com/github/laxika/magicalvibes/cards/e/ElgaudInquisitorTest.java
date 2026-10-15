package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElgaudInquisitor.class, WrathOfGod.class})
class ElgaudInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gains life for Elgaud Inquisitor's controller")
    void unblockedCombatDamageGainsLife() {
        addCreatureReady(player1, new ElgaudInquisitor());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Both Inquisitors gain life when trading in combat and each creates a Spirit")
    void lethalCombatDamageStillGainsLifeAndCreatesTokens() {
        addCreatureReady(player1, new ElgaudInquisitor());
        addCreatureReady(player2, new ElgaudInquisitor());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Elgaud Inquisitor");
        harness.assertInGraveyard(player2, "Elgaud Inquisitor");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
    }

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({ElgaudInquisitor.class, WrathOfGod.class})
    class DeathTriggerTests {

        @Test
        @DisplayName("When Elgaud Inquisitor dies, a 1/1 white Spirit token with flying is created")
        void deathTriggerCreatesSpiritToken() {
            harness.addToBattlefield(player1, new ElgaudInquisitor());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Elgaud Inquisitor dies

            // Elgaud Inquisitor should be in the graveyard
            harness.assertInGraveyard(player1, "Elgaud Inquisitor");

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
        @DisplayName("Elgaud Inquisitor death trigger belongs to its controller")
        void deathTriggerBelongsToController() {
            harness.addToBattlefield(player2, new ElgaudInquisitor());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Elgaud Inquisitor dies
            harness.passBothPriorities(); // Resolve death trigger

            // The Spirit token should be on player2's battlefield (the Elgaud Inquisitor's controller)
            List<Permanent> player2Tokens = findPermanents(player2, "Spirit");
            assertThat(player2Tokens).hasSize(1);

            // Player1 should have no Spirit tokens
            List<Permanent> player1Tokens = findPermanents(player1, "Spirit");
            assertThat(player1Tokens).isEmpty();
        }
    }
}
