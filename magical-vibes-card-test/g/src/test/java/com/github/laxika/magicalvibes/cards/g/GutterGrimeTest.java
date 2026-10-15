package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutterGrime.class, DarkthicketWolf.class, BlasphemousAct.class, IntangibleVirtue.class,
        Naturalize.class, Solemnity.class})
class GutterGrimeTest extends BaseCardTest {

    @CardUsed({GutterGrime.class, DarkthicketWolf.class, BlasphemousAct.class, IntangibleVirtue.class,
            Naturalize.class, Solemnity.class})
    @Nested
    @DisplayName("Triggered ability")
    class TriggeredAbilityTests {

        @Test
        @DisplayName("Gutter Grime survives: creates Ooze token with P/T equal to slime counters")
        void gutterGrimeSurvivesCreatesCorrectToken() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new DarkthicketWolf());

            // Kill the wolf with an opponent's Blasphemous Act so Gutter Grime (enchantment) survives
            harness.setHand(player2, List.of(new BlasphemousAct()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.forceActivePlayer(player2);

            harness.castAndResolveSorcery(player2, 0, 0); // Resolve Blasphemous Act — wolf dies

            // Gutter Grime trigger should be on the stack
            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities(); // Resolve the trigger

            // Gutter Grime should have 1 slime counter
            Permanent grime = findPermanent(player1, "Gutter Grime");
            assertThat(grime).isNotNull();
            assertThat(grime.getCounterCount(CounterType.SLIME)).isEqualTo(1);

            // One Ooze token should exist
            List<Permanent> oozes = findPermanents(player1, "Ooze");
            assertThat(oozes).hasSize(1);

            Permanent ooze = oozes.getFirst();
            assertThat(ooze.getCard().isToken()).isTrue();
            assertThat(ooze.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(ooze.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(ooze.getCard().getSubtypes()).contains(CardSubtype.OOZE);

            assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(1);
        }

        @Test
        @DisplayName("Multiple creature deaths produce multiple slime counters and tokens")
        void multipleDeathsMultipleTokens() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new DarkthicketWolf());
            harness.addToBattlefield(player1, new DarkthicketWolf());

            // Use opponent's Blasphemous Act so Gutter Grime survives
            harness.setHand(player2, List.of(new BlasphemousAct()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.forceActivePlayer(player2);

            harness.castAndResolveSorcery(player2, 0, 0); // Resolve Blasphemous Act — both wolves die

            // Two triggers on the stack
            assertThat(gd.stack).hasSize(2);

            harness.passBothPriorities(); // Resolve first trigger
            harness.passBothPriorities(); // Resolve second trigger

            // Gutter Grime should have 2 slime counters
            Permanent grime = findPermanent(player1, "Gutter Grime");
            assertThat(grime).isNotNull();
            assertThat(grime.getCounterCount(CounterType.SLIME)).isEqualTo(2);

            // Two Ooze tokens should exist
            List<Permanent> oozes = findPermanents(player1, "Ooze");
            assertThat(oozes).hasSize(2);

            for (Permanent ooze : oozes) {
                assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Does not trigger when a token creature dies")
        void doesNotTriggerOnTokenCreatureDeath() {
            harness.addToBattlefield(player1, new GutterGrime());

            harness.addToBattlefield(player1, new DarkthicketWolf());
            harness.setHand(player2, List.of(new BlasphemousAct()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.forceActivePlayer(player2);
            harness.castAndResolveSorcery(player2, 0, 0);
            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Ooze")).hasSize(1);

            // Use opponent's Blasphemous Act so Gutter Grime survives
            harness.setHand(player2, List.of(new BlasphemousAct()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.forceActivePlayer(player2);

            harness.castAndResolveSorcery(player2, 0, 0); // Resolve Blasphemous Act — token dies

            // No triggers should have fired — stack should be empty
            assertThat(gd.stack).isEmpty();

            // The token death must not add a slime counter
            Permanent grime = findPermanent(player1, "Gutter Grime");
            assertThat(grime).isNotNull();
            assertThat(grime.getCounterCount(CounterType.SLIME)).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not trigger when opponent's nontoken creature dies")
        void doesNotTriggerOnOpponentCreatureDeath() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player2, new DarkthicketWolf());

            // Use player1's Blasphemous Act so Gutter Grime (enchantment) survives, opponent's wolf dies
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 9);

            harness.castAndResolveSorcery(player1, 0, 0); // Resolve Blasphemous Act — opponent's wolf dies

            // Gutter Grime should have 0 slime counters
            Permanent grime = findPermanent(player1, "Gutter Grime");
            assertThat(grime).isNotNull();
            assertThat(grime.getCounterCount(CounterType.SLIME)).isEqualTo(0);
        }

        @Test
        @DisplayName("Creates a token even if Gutter Grime leaves before resolution")
        void createsTokenWhenGutterGrimeDestroyed() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new IntangibleVirtue());
            harness.addToBattlefield(player1, new DarkthicketWolf());
            harness.setHand(player2, List.of(new BlasphemousAct(), new Naturalize()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.forceActivePlayer(player2);
            harness.castAndResolveSorcery(player2, 0, 0);
            assertThat(gd.stack).hasSize(1);

            harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Gutter Grime").getId());
            assertThat(findPermanents(player1, "Gutter Grime")).isEmpty();
            harness.passBothPriorities();

            List<Permanent> oozes = findPermanents(player1, "Ooze");
            assertThat(oozes).hasSize(1);
            assertThat(gqs.getEffectivePower(gd, oozes.getFirst())).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, oozes.getFirst())).isEqualTo(1);
        }

        @Test
        @CardUsed({GutterGrime.class, DarkthicketWolf.class, BlasphemousAct.class,
                IntangibleVirtue.class, Solemnity.class})
        @DisplayName("Counter prevention does not prevent token creation")
        void createsTokenWhenSlimeCounterCannotBePlaced() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new IntangibleVirtue());
            harness.addToBattlefield(player1, new Solemnity());
            harness.addToBattlefield(player1, new DarkthicketWolf());
            harness.setHand(player2, List.of(new BlasphemousAct()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.forceActivePlayer(player2);
            harness.castAndResolveSorcery(player2, 0, 0);
            harness.passBothPriorities();

            assertThat(findPermanent(player1, "Gutter Grime").getCounterCount(CounterType.SLIME)).isZero();
            List<Permanent> oozes = findPermanents(player1, "Ooze");
            assertThat(oozes).hasSize(1);
            assertThat(gqs.getEffectivePower(gd, oozes.getFirst())).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, oozes.getFirst())).isEqualTo(1);
        }

        @Test
        @DisplayName("Existing tokens become 0/0 when their source leaves")
        void existingTokensLoseBasePowerAndToughnessWhenSourceLeaves() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new IntangibleVirtue());
            harness.addToBattlefield(player1, new DarkthicketWolf());
            harness.setHand(player2, List.of(new BlasphemousAct(), new Naturalize()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.forceActivePlayer(player2);
            harness.castAndResolveSorcery(player2, 0, 0);
            harness.passBothPriorities();
            Permanent ooze = findPermanent(player1, "Ooze");
            assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(2);

            harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Gutter Grime").getId());

            assertThat(findPermanents(player1, "Ooze")).containsExactly(ooze);
            assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(1);
        }

        @Test
        @DisplayName("Tokens refer only to the Gutter Grime that created them")
        void tokensTrackTheirOwnSourceOnly() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new DarkthicketWolf());
            List<Permanent> grimes = findPermanents(player1, "Gutter Grime");
            grimes.getFirst().setCounterCount(CounterType.SLIME, 2);
            harness.setHand(player2, List.of(new BlasphemousAct()));
            harness.addMana(player2, ManaColor.RED, 9);
            harness.forceActivePlayer(player2);
            harness.castAndResolveSorcery(player2, 0, 0);
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(grimes.getFirst().getCounterCount(CounterType.SLIME)).isEqualTo(3);
            assertThat(grimes.getLast().getCounterCount(CounterType.SLIME)).isEqualTo(1);
            List<Permanent> oozes = findPermanents(player1, "Ooze");
            assertThat(oozes).hasSize(2);
            assertThat(oozes.stream().map(ooze -> gqs.getEffectivePower(gd, ooze)).toList())
                    .containsExactlyInAnyOrder(1, 3);
            assertThat(oozes.stream().map(ooze -> gqs.getEffectiveToughness(gd, ooze)).toList())
                    .containsExactlyInAnyOrder(1, 3);
        }
    }
}
