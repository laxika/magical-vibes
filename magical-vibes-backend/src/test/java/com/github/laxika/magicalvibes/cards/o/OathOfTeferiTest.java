package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OathOfTeferiTest extends BaseCardTest {

    // ===== ETB: Exile another target permanent you control =====

    @Nested
    @DisplayName("ETB exile and return")
    class EtbExileAndReturn {

        @Test
        @DisplayName("ETB exiles target permanent you control")
        void etbExilesTargetPermanent() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, bearsId);
            // Resolve enchantment spell
            harness.passBothPriorities();
            // Resolve ETB trigger
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Exiled permanent returns at beginning of next end step")
        void exiledPermanentReturnsAtEndStep() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, bearsId);
            harness.passBothPriorities(); // resolve spell
            harness.passBothPriorities(); // resolve ETB

            // Bears should be exiled
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

            // Advance to end step
            advanceToEndStep();

            // Bears should be back on battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Returned permanent has summoning sickness")
        void returnedPermanentHasSummoningSickness() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, bearsId);
            harness.passBothPriorities(); // resolve spell
            harness.passBothPriorities(); // resolve ETB

            advanceToEndStep();

            Permanent returnedBears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            assertThat(returnedBears.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("ETB fizzles if target is removed before resolution")
        void etbFizzlesIfTargetRemoved() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, bearsId);
            harness.passBothPriorities(); // resolve enchantment spell → ETB on stack

            // Remove target before ETB resolves
            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

            harness.passBothPriorities(); // resolve ETB → fizzles

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingExileReturns).isEmpty();
        }
    }

    // ===== Target restrictions =====

    @Nested
    @DisplayName("Target restrictions")
    class TargetRestrictions {

        @Test
        @DisplayName("Cannot target opponent's permanent")
        void cannotTargetOpponentPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBearsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Static: Double loyalty activation =====

    @Nested
    @DisplayName("Double loyalty activation")
    class DoubleLoyaltyActivation {

        @Test
        @DisplayName("With Oath of Teferi, can activate planeswalker loyalty ability twice per turn")
        void canActivateLoyaltyTwiceWithOath() {
            // Put Oath of Teferi on battlefield (index 0)
            harness.addToBattlefield(player1, new OathOfTeferi());

            // Add Garruk Wildspeaker (index 1)
            Permanent garruk = addReadyPlaneswalker(player1);
            garruk.setLoyaltyCounters(5);

            // First activation: -1 (create Beast token) — permanentIndex=1 for Garruk, abilityIndex=1 for -1
            harness.activateAbility(player1, 1, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(4);

            // Second activation in same turn: -1 again
            harness.activateAbility(player1, 1, 1, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getLoyaltyCounters()).isEqualTo(3);

            // Verify two Beast tokens were created
            long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Beast"))
                    .count();
            assertThat(tokenCount).isEqualTo(2);
        }

        @Test
        @DisplayName("With Oath of Teferi, third activation still fails")
        void thirdActivationFailsWithOath() {
            harness.addToBattlefield(player1, new OathOfTeferi());

            Permanent garruk = addReadyPlaneswalker(player1);
            garruk.setLoyaltyCounters(10);

            // First activation
            harness.activateAbility(player1, 1, 1, null, null);
            harness.passBothPriorities();

            // Second activation
            harness.activateAbility(player1, 1, 1, null, null);
            harness.passBothPriorities();

            // Third activation should fail
            assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("loyalty");
        }

        @Test
        @DisplayName("Without Oath of Teferi, only one activation per turn")
        void onlyOneActivationWithoutOath() {
            Permanent garruk = addReadyPlaneswalker(player1);
            garruk.setLoyaltyCounters(5);

            // First activation
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            // Second activation should fail without Oath
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("loyalty");
        }

        @Test
        @DisplayName("Removing Oath of Teferi mid-turn restores normal limit")
        void removingOathRestoresNormalLimit() {
            Permanent oath = new Permanent(new OathOfTeferi());
            oath.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(oath);

            // Garruk at index 1 (oath is at index 0)
            Permanent garruk = addReadyPlaneswalker(player1);
            garruk.setLoyaltyCounters(10);

            // First activation with Oath present
            harness.activateAbility(player1, 1, 1, null, null);
            harness.passBothPriorities();

            // Remove Oath from battlefield
            gd.playerBattlefields.get(player1.getId()).remove(oath);

            // Garruk is now at index 0 after Oath removal
            // Second activation should fail (already used once, and Oath is gone)
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("loyalty");
        }
    }

    // ===== Helpers =====

    private Permanent addReadyPlaneswalker(Player player) {
        GarrukWildspeaker card = new GarrukWildspeaker();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
