package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({MitoticSlime.class, DayOfJudgment.class, Unsummon.class})
class MitoticSlimeTest extends BaseCardTest {

    @CardUsed({MitoticSlime.class, DayOfJudgment.class})
    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Mitotic Slime dies, two 2/2 green Ooze tokens are created")
        void deathTriggerCreatesTwoTokens() {
            harness.addToBattlefield(player1, new MitoticSlime());

            harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Day of Judgment — Slime dies

            GameData gd = harness.getGameData();

            // Mitotic Slime should be in the graveyard
            harness.assertInGraveyard(player1, "Mitotic Slime");

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            // Two 2/2 Ooze tokens should be on the battlefield
            List<Permanent> tokens = findPermanents(player1, "Ooze");
            assertThat(tokens).hasSize(2);

            for (Permanent token : tokens) {
                assertThat(token.getCard().getPower()).isEqualTo(2);
                assertThat(token.getCard().getToughness()).isEqualTo(2);
                assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                assertThat(token.getCard().getSubtypes()).contains(CardSubtype.OOZE);
                assertThat(token.getCard().isToken()).isTrue();
            }
        }

        @Test
        @DisplayName("2/2 Ooze tokens have their own death trigger that creates 1/1 Ooze tokens")
        void oozeTokensHaveDeathTrigger() {
            harness.addToBattlefield(player1, new MitoticSlime());

            // Kill Mitotic Slime with Day of Judgment
            harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Day of Judgment — Slime dies
            harness.passBothPriorities(); // Resolve Slime death trigger — two 2/2 Ooze tokens enter

            GameData gd = harness.getGameData();

            assertThat(findPermanents(player1, "Ooze")).hasSize(2);
            harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(2);

            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Ooze")).hasSize(2)
                    .allSatisfy(token -> assertThat(token.getCard().getPower()).isEqualTo(1));
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Ooze")).hasSize(4);
        }

        @Test
        @DisplayName("Killing a 2/2 Ooze token creates two 1/1 Ooze tokens")
        void killingOozeTokenCreatesSmallTokens() {
            harness.addToBattlefield(player1, new MitoticSlime());

            // Kill Mitotic Slime with Day of Judgment
            harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Day of Judgment — Slime dies
            harness.passBothPriorities(); // Resolve Slime death trigger — two 2/2 Ooze tokens enter

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze") && p.getCard().getPower() == 2)
                    .count()).isEqualTo(2);

            // Now kill all creatures again with another Day of Judgment
            harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Day of Judgment — both 2/2 Ooze tokens die

            // Two death triggers should be on the stack (one per 2/2 token)
            assertThat(gd.stack).hasSize(2);

            // Resolve both death triggers
            harness.passBothPriorities();
            harness.passBothPriorities();

            // Four 1/1 Ooze tokens should now be on the battlefield
            List<Permanent> smallTokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze") && p.getCard().getPower() == 1)
                    .toList();
            assertThat(smallTokens).hasSize(4);

            for (Permanent smallToken : smallTokens) {
                assertThat(smallToken.getCard().getPower()).isEqualTo(1);
                assertThat(smallToken.getCard().getToughness()).isEqualTo(1);
                assertThat(smallToken.getCard().getColor()).isEqualTo(CardColor.GREEN);
                assertThat(smallToken.getCard().getSubtypes()).contains(CardSubtype.OOZE);
                assertThat(smallToken.getCard().isToken()).isTrue();
            }

            harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
            harness.passBothPriorities();
            assertThat(gd.stack).isEmpty();
            assertThat(findPermanents(player1, "Ooze")).isEmpty();
        }
    }

    @Test
    @DisplayName("Returning Mitotic Slime to hand does not create Oozes")
    void returningSlimeToHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new MitoticSlime());
        Permanent slime = findPermanent(player1, "Mitotic Slime");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, slime.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mitotic Slime");
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The dying Slime's controller creates the tokens")
    void opponentControlledSlimeCreatesTokensForOpponent() {
        harness.addToBattlefield(player2, new MitoticSlime());
        harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player2, "Ooze")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Ooze")).hasSize(2);
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
    }
}
