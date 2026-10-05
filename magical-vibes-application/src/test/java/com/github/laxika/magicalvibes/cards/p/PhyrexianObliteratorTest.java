package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ActOfAggression;
import com.github.laxika.magicalvibes.cards.c.ChancellorOfTheTangle;
import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.s.ShrineOfBurningRage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianObliterator.class, GrizzlyBears.class, Shock.class, ActOfAggression.class,
        ChancellorOfTheTangle.class, GlistenerElf.class, PristineTalisman.class, ShrineOfBurningRage.class})
class PhyrexianObliteratorTest extends BaseCardTest {

    @Test
    @DisplayName("Shock dealing 2 damage to Obliterator forces source controller to sacrifice 2 permanents")
    void spellDamageForcesSourceControllerToSacrifice() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID obliteratorId = harness.getPermanentId(player2, "Phyrexian Obliterator");
        harness.castAndResolveInstant(player1, 0, obliteratorId); // Resolve Shock — 2 damage to Obliterator

        GameData gd = harness.getGameData();

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger — player1 has exactly 2 permanents, so both are auto-sacrificed
        harness.passBothPriorities();

        // Both Grizzly Bears should be sacrificed (battlefield empty)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // Obliterator should survive (5/5 takes only 2 damage)
        harness.assertOnBattlefield(player2, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("When source controller has more permanents than damage, they choose which to sacrifice")
    void spellDamagePromptsChoiceWhenMorePermanents() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID obliteratorId = harness.getPermanentId(player2, "Phyrexian Obliterator");
        harness.castAndResolveInstant(player1, 0, obliteratorId); // Resolve Shock — 2 damage

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger — player1 has 3 permanents but must sacrifice 2
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        // Player1 chooses which two to sacrifice
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        UUID first = p1Battlefield.get(0).getId();
        UUID second = p1Battlefield.get(1).getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(first, second));

        // Two sacrificed, one remains
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        // Graveyard has Shock + 2 sacrificed Grizzly Bears
        long sacrificedBears = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count();
        assertThat(sacrificedBears).isEqualTo(2);
    }

    @Test
    @DisplayName("When source controller has no permanents, nothing happens")
    void noPermanentsToSacrifice() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID obliteratorId = harness.getPermanentId(player2, "Phyrexian Obliterator");
        harness.castAndResolveInstant(player1, 0, obliteratorId); // Resolve Shock

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger — player1 has no permanents
        harness.passBothPriorities();

        // Nothing to sacrifice, game continues normally
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocking creature dealing combat damage forces attacker's controller to sacrifice permanents")
    void combatDamageForcesAttackerControllerToSacrifice() {
        Permanent obliterator = addCreatureReady(player2, new PhyrexianObliterator());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears()); // extra permanent

        attacker.setAttacking(true);

        obliterator.setBlocking(true);
        obliterator.addBlockingTarget(0);

        // Resolve combat damage — attacker (2/2) deals 2 to Obliterator, Obliterator (5/5) kills attacker
        // Obliterator trigger goes on stack for 2 damage
        resolveCombat();

        // After combat, attacker dies (lethal from Obliterator), trigger on stack
        // One Grizzly Bears remains. Sacrifice count = 2, but only 1 permanent → auto-sacrifice that one
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Player1 should have no permanents (attacker died in combat + remaining auto-sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // Obliterator should survive (5/5 takes 2 damage)
        harness.assertOnBattlefield(player2, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("Obliterator as attacker blocked by creature triggers sacrifice on blocker's controller")
    void obliteratorAttackingBlockedTriggersOnBlockerController() {
        Permanent obliterator = addCreatureReady(player1, new PhyrexianObliterator());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears()); // additional permanent
        harness.addToBattlefield(player2, new GrizzlyBears()); // additional permanent

        obliterator.setAttacking(true);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Advance from DECLARE_BLOCKERS — Obliterator has trample so manual damage assignment needed
        resolveCombat();

        // Assign Obliterator's 5 damage: 2 to blocker (lethal for 2/2), 3 trample to player2
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        GameData gd = harness.getGameData();

        // Obliterator trigger fires: Grizzly Bears dealt 2 to Obliterator → player2 sacrifices 2
        // Blocker died in combat, player2 has 2 remaining → auto-sacrifice both
        // Resolve combat damage triggers
        harness.passBothPriorities();

        // All of player2's permanents should be gone (blocker died + 2 sacrificed)
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        // Obliterator should survive (5/5 takes 2 damage)
        harness.assertOnBattlefield(player1, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("Lethal damage still triggers and uses the sacrificed damage source's last controller")
    void lethalDamageFromSacrificedSourceStillForcesSacrifice() {
        Permanent obliterator = harness.addToBattlefieldAndReturn(player2, new PhyrexianObliterator());
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new ShrineOfBurningRage());
        shrine.setCounterCount(CounterType.CHARGE, 7);
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, obliterator.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Obliterator");
        harness.assertInGraveyard(player1, "Shrine of Burning Rage");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Pristine Talisman")).hasSize(3);
    }

    @Test
    @DisplayName("Zero damage does not trigger a sacrifice")
    void zeroDamageDoesNotTrigger() {
        Permanent obliterator = harness.addToBattlefieldAndReturn(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new ShrineOfBurningRage());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, obliterator.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Pristine Talisman");
        harness.assertOnBattlefield(player2, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("Damage from your own source can make you sacrifice Obliterator itself")
    void ownDamageSourceCanForceSacrificeOfObliterator() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new ShrineOfBurningRage());
        shrine.setCounterCount(CounterType.CHARGE, 2);
        Permanent obliterator = harness.addToBattlefieldAndReturn(player1, new PhyrexianObliterator());
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, obliterator.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(obliterator.getId(), talisman.getId()));

        harness.assertInGraveyard(player1, "Phyrexian Obliterator");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Pristine Talisman");
    }

    @Test
    @DisplayName("Two sources dealing infect damage simultaneously create separate sacrifice triggers")
    void simultaneousInfectSourcesCreateSeparateTriggers() {
        Permanent obliterator = addCreatureReady(player1, new PhyrexianObliterator());
        Permanent firstElf = addCreatureReady(player2, new GlistenerElf());
        Permanent secondElf = addCreatureReady(player2, new GlistenerElf());
        Permanent firstTalisman = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());
        harness.addToBattlefield(player2, new PristineTalisman());
        obliterator.setAttacking(true);
        firstElf.setBlocking(true);
        firstElf.addBlockingTarget(0);
        secondElf.setBlocking(true);
        secondElf.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstElf.getId(), 1, secondElf.getId(), 1, player2.getId(), 3));

        assertThat(obliterator.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(firstTalisman.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("Damage source's current controller sacrifices if control changes before resolution")
    void changedDamageSourceControllerSacrificesAtResolution() {
        Permanent chancellor = addCreatureReady(player1, new ChancellorOfTheTangle());
        Permanent obliterator = addCreatureReady(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new PristineTalisman());
        chancellor.setAttacking(true);
        obliterator.setBlocking(true);
        obliterator.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Phyrexian Obliterator");
        harness.assertOnBattlefield(player1, "Chancellor of the Tangle");
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new ActOfAggression()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, chancellor.getId());
        harness.assertOnBattlefield(player2, "Chancellor of the Tangle");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chancellor of the Tangle");
        harness.assertInGraveyard(player1, "Chancellor of the Tangle");
        harness.assertOnBattlefield(player1, "Pristine Talisman");
    }
}
