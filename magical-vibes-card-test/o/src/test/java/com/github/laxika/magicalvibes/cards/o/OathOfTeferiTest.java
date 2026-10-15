package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OathOfTeferi.class, BalothGorger.class, KarnScionOfUrza.class, DeepFreeze.class})
class OathOfTeferiTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB exile and return")
    @CardUsed({OathOfTeferi.class, BalothGorger.class})
    class EtbExileAndReturn {

        @Test
        @DisplayName("ETB exiles target permanent you control")
        void etbExilesTargetPermanent() {
            harness.addToBattlefield(player1, new BalothGorger());
            UUID creatureId = harness.getPermanentId(player1, "Baloth Gorger");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, creatureId);
            // Resolve enchantment spell
            harness.passBothPriorities();
            // Resolve ETB trigger
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Baloth Gorger");
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Baloth Gorger"));
        }

        @Test
        @DisplayName("Exiled permanent returns at beginning of next end step")
        void exiledPermanentReturnsAtEndStep() {
            harness.addToBattlefield(player1, new BalothGorger());
            UUID creatureId = harness.getPermanentId(player1, "Baloth Gorger");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, creatureId);
            harness.passBothPriorities(); // resolve spell
            harness.passBothPriorities(); // resolve ETB

            // Creature should be exiled
            harness.assertNotOnBattlefield(player1, "Baloth Gorger");

            // Advance to end step
            advanceToEndStep();

            // Creature should be back on battlefield
            harness.assertOnBattlefield(player1, "Baloth Gorger");
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Baloth Gorger"));
        }

        @Test
        @DisplayName("Returned permanent has summoning sickness")
        void returnedPermanentHasSummoningSickness() {
            harness.addToBattlefield(player1, new BalothGorger());
            UUID creatureId = harness.getPermanentId(player1, "Baloth Gorger");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, creatureId);
            harness.passBothPriorities(); // resolve spell
            harness.passBothPriorities(); // resolve ETB

            advanceToEndStep();

            Permanent returnedCreature = findPermanent(player1, "Baloth Gorger");
            assertThat(returnedCreature.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("ETB fizzles if target is removed before resolution")
        void etbFizzlesIfTargetRemoved() {
            harness.addToBattlefield(player1, new BalothGorger());
            UUID creatureId = harness.getPermanentId(player1, "Baloth Gorger");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0, creatureId);
            harness.passBothPriorities(); // resolve enchantment spell → ETB on stack

            // Remove target before ETB resolves
            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getCard().getName().equals("Baloth Gorger"));

            harness.passBothPriorities(); // resolve ETB → fizzles

            assertThat(gd.stack).isEmpty();
            assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Target restrictions")
    @CardUsed({OathOfTeferi.class, BalothGorger.class})
    class TargetRestrictions {

        @Test
        @DisplayName("Cannot target opponent's permanent")
        void cannotTargetOpponentPermanent() {
            harness.addToBattlefield(player2, new BalothGorger());
            UUID opponentCreatureId = harness.getPermanentId(player2, "Baloth Gorger");

            harness.setHand(player1, List.of(new OathOfTeferi()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.addMana(player1, ManaColor.BLUE, 2);

            assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreatureId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Double loyalty activation")
    @CardUsed({OathOfTeferi.class, KarnScionOfUrza.class})
    class DoubleLoyaltyActivation {

        @Test
        @DisplayName("With Oath of Teferi, can activate planeswalker loyalty ability twice per turn")
        void canActivateLoyaltyTwiceWithOath() {
            // Put Oath of Teferi on battlefield (index 0)
            harness.addToBattlefield(player1, new OathOfTeferi());

            // Add Karn, Scion of Urza (index 1)
            Permanent karn = addReadyPlaneswalker(player1);
            karn.setCounterCount(CounterType.LOYALTY, 5);

            // Karn's -2 ability creates a Construct.
            harness.activateAbility(player1, 1, 2, null, null);
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

            // Second activation in same turn: -2 again
            harness.activateAbility(player1, 1, 2, null, null);
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

            // Verify two Construct tokens were created
            long tokenCount = countPermanents(player1, "Construct");
            assertThat(tokenCount).isEqualTo(2);
        }

        @Test
        @DisplayName("With Oath of Teferi, third activation still fails")
        void thirdActivationFailsWithOath() {
            harness.addToBattlefield(player1, new OathOfTeferi());

            Permanent karn = addReadyPlaneswalker(player1);
            karn.setCounterCount(CounterType.LOYALTY, 10);

            // First activation
            harness.activateAbility(player1, 1, 2, null, null);
            harness.passBothPriorities();

            // Second activation
            harness.activateAbility(player1, 1, 2, null, null);
            harness.passBothPriorities();

            // Third activation should fail
            assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("loyalty");
        }

        @Test
        @DisplayName("Without Oath of Teferi, only one activation per turn")
        void onlyOneActivationWithoutOath() {
            Permanent karn = addReadyPlaneswalker(player1);
            karn.setCounterCount(CounterType.LOYALTY, 5);

            // First activation
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            // Second activation should fail without Oath
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("loyalty");
        }

        @Test
        @DisplayName("Removing Oath of Teferi mid-turn restores normal limit")
        void removingOathRestoresNormalLimit() {
            Permanent oath = harness.addToBattlefieldAndReturn(player1, new OathOfTeferi());

            // Karn at index 1 (oath is at index 0)
            Permanent karn = addReadyPlaneswalker(player1);
            karn.setCounterCount(CounterType.LOYALTY, 10);

            // First activation with Oath present
            harness.activateAbility(player1, 1, 2, null, null);
            harness.passBothPriorities();

            // Remove Oath from battlefield
            gd.playerBattlefields.get(player1.getId()).remove(oath);

            // Karn is now at index 0 after Oath removal
            // Second activation should fail (already used once, and Oath is gone)
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("loyalty");
        }
    }

    private Permanent addReadyPlaneswalker(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KarnScionOfUrza());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void castAndResolveOath(UUID targetId) {
        harness.setHand(player1, List.of(new OathOfTeferi()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void returnSurvivesOathLeavingBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        castAndResolveOath(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Oath of Teferi"));
        advanceToEndStep();
        harness.assertOnBattlefield(player1, "Baloth Gorger");
    }

    @Test
    void stolenPermanentReturnsToItsOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        castAndResolveOath(creature.getId());
        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertOnBattlefield(player2, "Baloth Gorger");
    }

    @Test
    void targetThatChangesControllerBeforeResolutionIsNotExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new OathOfTeferi()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Baloth Gorger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void returnAbilityUsesTheStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        castAndResolveOath(creature.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Baloth Gorger");
    }

    @Test
    void oathDoesNotAllowLoyaltyActivationOutsideMainPhase() {
        harness.addToBattlefield(player1, new OathOfTeferi());
        addReadyPlaneswalker(player1);
        harness.forceStep(TurnStep.END_STEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsOathDoesNotGrantAnExtraActivation() {
        harness.addToBattlefield(player2, new OathOfTeferi());
        addReadyPlaneswalker(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("loyalty");
    }

    @Test
    @CardUsed({OathOfTeferi.class, BalothGorger.class, DeepFreeze.class})
    void returnedAuraCanAttachToALegalCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        aura.setAttachedTo(creature.getId());
        castAndResolveOath(aura.getId());
        advanceToEndStep();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.assertOnBattlefield(player1, "Deep Freeze");
        assertThat(findPermanent(player1, "Deep Freeze").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @CardUsed({OathOfTeferi.class, BalothGorger.class, DeepFreeze.class})
    void auraRemainsExiledWhenNothingCanBeEnchanted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        aura.setAttachedTo(creature.getId());
        castAndResolveOath(aura.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Deep Freeze");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Deep Freeze"));
        harness.assertNotInGraveyard(player1, "Deep Freeze");
    }
    @Test
    void exiledTokenDoesNotReturn() {
        addReadyPlaneswalker(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Construct");
        castAndResolveOath(token.getId());
        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Construct");
    }

    @Test
    void differentLoyaltyAbilitiesShareTheTwoActivationLimit() {
        harness.addToBattlefield(player1, new OathOfTeferi());
        Permanent karn = addReadyPlaneswalker(player1);
        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("loyalty");
    }

    @Test
    void oathCanResolveWithoutAnotherPermanentToTarget() {
        harness.setHand(player1, List.of(new OathOfTeferi()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oath of Teferi");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void delayedReturnDoesNotReturnACardThatLeftExileAndWasExiledAgain() {
        BalothGorger card = new BalothGorger();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        castAndResolveOath(creature.getId());
        assertThat(gd.removeFromExile(card.getId())).isTrue();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent recastCreature = findPermanent(player1, "Baloth Gorger");
        harness.getPermanentRemovalService().removePermanentToExile(gd, recastCreature);
        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getId().equals(card.getId()));
    }
}
