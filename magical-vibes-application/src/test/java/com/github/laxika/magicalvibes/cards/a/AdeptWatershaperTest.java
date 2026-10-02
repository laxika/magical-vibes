package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdeptWatershaper.class, GrizzlyBears.class, Assassinate.class, WrathOfGod.class})
class AdeptWatershaperTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Adept Watershaper puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Adept Watershaper onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Adept Watershaper");
    }

    @Test
    @DisplayName("Adept Watershaper enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Adept Watershaper");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Tapped creature you control gains indestructible")
    void tappedCreatureGainsIndestructible() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Untapped creature you control does not gain indestructible")
    void untappedCreatureDoesNotGainIndestructible() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(bears.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Adept Watershaper does not grant indestructible to itself")
    void doesNotGrantIndestructibleToItself() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent watershaper = findPermanent(player1, "Adept Watershaper");
        watershaper.tap();

        assertThat(gqs.hasKeyword(gd, watershaper, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant indestructible to opponent's tapped creatures")
    void doesNotGrantIndestructibleToOpponentCreatures() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        opponentBears.tap();

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses indestructible when it untaps")
    void creatureLosesIndestructibleWhenUntapped() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        bears.untap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible is removed when Adept Watershaper leaves the battlefield")
    void indestructibleRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Remove Watershaper from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Adept Watershaper"));

        // Indestructible should be gone immediately (computed on the fly)
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Static indestructible survives end-of-turn modifier reset")
    void staticIndestructibleSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Simulate end-of-turn cleanup
        bears.resetModifiers();

        // Static keyword should still be computed
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Two Watershapers grant indestructible to each other when tapped")
    void twoWatershapersGrantIndestructibleToEachOther() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new AdeptWatershaper());

        List<Permanent> watershapers = findPermanents(player1, "Adept Watershaper");

        assertThat(watershapers).hasSize(2);

        // Tap both
        watershapers.get(0).tap();
        watershapers.get(1).tap();

        // Each should be indestructible from the other
        for (Permanent ws : watershapers) {
            assertThat(gqs.hasKeyword(gd, ws, Keyword.INDESTRUCTIBLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Indestructible tapped creature survives targeted destroy effect")
    void indestructibleSurvivesTargetedDestroy() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent tappedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedBears.tap();

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Assassinate targeting the tapped creature
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, tappedBears.getId());

        // Creature should survive — still on battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        // Log should indicate indestructible
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("indestructible"));
    }

    @Test
    @DisplayName("Indestructible tapped creature survives Wrath of God")
    void indestructibleSurvivesWrathOfGod() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent tappedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedBears.tap();

        // Opponent has a creature too
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Wrath of God
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        // The tapped bears should survive (indestructible)
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Watershaper was untapped → NOT indestructible → should be destroyed
        harness.assertNotOnBattlefield(player1, "Adept Watershaper");

        // Opponent's bears should be destroyed (not protected)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped creature without Watershaper is NOT protected from destroy effects")
    void tappedCreatureWithoutWatershaperNotProtected() {
        // No Watershaper on battlefield — tapped creature has no indestructible
        Permanent tappedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedBears.tap();

        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isFalse();

        // Cast Assassinate
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, tappedBears.getId());

        // Creature should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped attacker with indestructible survives lethal combat damage")
    void indestructibleAttackerSurvivesCombatDamage() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        // The 2/2 will take lethal damage from a 3/4.
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap(); // Attackers are tapped → indestructible from Watershaper

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();

        // The opposing Watershaper deals lethal damage to the Bears.
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AdeptWatershaper());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1); // Index 1 (Watershaper is 0, attacker is 1)

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage
        harness.passBothPriorities();

        // Attacker should survive (indestructible)
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        // The 3/4 blocker survives the two damage dealt by the Bears.
        harness.assertOnBattlefield(player2, "Adept Watershaper");
    }

    @Test
    @DisplayName("Indestructible blocker survives lethal combat damage from attacker")
    void indestructibleBlockerSurvivesCombatDamage() {
        harness.addToBattlefield(player2, new AdeptWatershaper());

        // The 2/2 blocker will take lethal damage from a 3/4.
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.tap(); // Represents tapping the blocker after it was legally declared.

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isTrue();

        // The opposing 3/4 deals lethal damage to the Bears.
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AdeptWatershaper());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage
        harness.passBothPriorities();

        // Blocker should survive (indestructible)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking creatures are tapped and thus gain indestructible from Watershaper")
    void attackingCreatureGainsIndestructibleFromTapping() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);

        // Before attacking — untapped, not indestructible
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();

        // Declare attackers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1)); // Index 1 (Watershaper at 0)

        // After declaring attackers — tapped and indestructible
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible applies when Adept Watershaper resolves onto battlefield")
    void indestructibleAppliesOnResolve() {
        // Existing tapped creature
        Permanent tappedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedBears.tap();

        // Before Watershaper — no indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isFalse();

        // Cast and resolve Watershaper
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // After resolving, tapped creature should be indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Untapping a protected creature with lethal damage causes it to die")
    void lethalDamageKillsAfterUntapping() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        bears.untap();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lethal damage kills a protected creature after Watershaper dies")
    void lethalDamageKillsAfterSourceDies() {
        Permanent watershaper = harness.addToBattlefieldAndReturn(player1, new AdeptWatershaper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        bears.setMarkedDamage(2);
        watershaper.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Adept Watershaper");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible does not save a creature with zero toughness")
    void zeroToughnessStillDies() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        bears.setToughnessModifier(-2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}

