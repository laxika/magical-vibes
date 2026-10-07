package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FallOfTheHammer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRollercrusherRide.class, Shock.class, FallOfTheHammer.class,
        GrizzlyBears.class, AirElemental.class, MindStone.class, Forest.class})
class TheRollercrusherRideTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each of up to X target creatures")
    void dealsXDamageToEachOfUpToXTargetCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheRollercrusherRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> creatures = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.handlePermanentChosen(player1, creatures.get(0).getId());
        harness.handlePermanentChosen(player1, creatures.get(1).getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not double noncombat damage without delirium")
    void doesNotDoubleNoncombatDamageWithoutDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Doubles noncombat damage with delirium")
    void doublesNoncombatDamageWithDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Doubles damage dealt by a creature you control with delirium")
    void doublesDamageDealtByControlledCreatureWithDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void doublesItsOwnEnterDamageWithDelirium() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new TheRollercrusherRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canChooseNoTargetsForPositiveX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheRollercrusherRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "The Rollercrusher Ride");
    }

    @Test
    void zeroXEntersWithoutDamagingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheRollercrusherRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "The Rollercrusher Ride");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotDoubleOpponentsDamageEvenWhenBothPlayersHaveDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void checksDeliriumWhenDamageWouldBeDealt() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new MindStone(), new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotDoubleCombatDamageWithDelirium() {
        harness.addToBattlefield(player1, new TheRollercrusherRide());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new Shock()));
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
    }

    @Test
    void stillDamagesRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TheRollercrusherRide(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canTargetMoreThanOneHundredCreaturesWhenXIsLarger() {
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId());
        }
        harness.setHand(player1, List.of(new TheRollercrusherRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 103);

        gs.playCard(gd, player1, 0, 101, null, null);
        harness.passBothPriorities();
        for (UUID target : targets) {
            harness.handlePermanentChosen(player1, target);
        }
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
