package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({EtherswornShieldmage.class, GrizzlyBears.class, Ornithopter.class, Shock.class})
class EtherswornShieldmageTest extends BaseCardTest {

    private void castShieldmage() {
        harness.setHand(player1, List.of(new EtherswornShieldmage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void shock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Prevents all damage to an artifact creature this turn")
    void protectsArtifactCreature() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShieldmage();
        // Shock's 2 damage would be lethal to the 0/2 artifact creature, but it's prevented.
        shock(ornithopter);

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Does not prevent damage to a non-artifact creature")
    void doesNotProtectNonArtifactCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShieldmage();
        shock(bears);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevention wears off after turn cleanup")
    void wearsOff() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShieldmage();
        harness.passUntil(player2, TurnStep.UPKEEP);

        shock(ornithopter);

        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Also protects artifact creatures controlled by an opponent")
    void protectsOpponentArtifactCreature() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castShieldmage();
        shock(ornithopter);

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Protects artifact creatures that enter after the trigger resolves")
    void protectsLaterArtifactCreature() {
        castShieldmage();
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        shock(findPermanent(player1, "Ornithopter"));

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Protects itself from repeated damage events")
    void protectsItselfRepeatedly() {
        castShieldmage();
        Permanent shieldmage = findPermanent(player1, "Ethersworn Shieldmage");

        shock(shieldmage);
        shock(shieldmage);

        harness.assertOnBattlefield(player1, "Ethersworn Shieldmage");
        harness.assertNotInGraveyard(player1, "Ethersworn Shieldmage");
    }

    @Test
    @DisplayName("Damage can kill Shieldmage before its trigger resolves, but the trigger still protects others")
    void triggerWorksAfterSourceDiesInResponse() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new EtherswornShieldmage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        shock(findPermanent(player1, "Ethersworn Shieldmage"));
        harness.assertInGraveyard(player1, "Ethersworn Shieldmage");
        resolveAllTriggers();
        shock(ornithopter);

        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Flash allows Shieldmage to protect creatures during an opponent's turn")
    void canBeCastDuringOpponentTurn() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.passUntil(player2, TurnStep.UPKEEP);

        castShieldmage();
        shock(ornithopter);

        harness.assertOnBattlefield(player1, "Ethersworn Shieldmage");
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Prevention does not protect players")
    void doesNotProtectPlayers() {
        castShieldmage();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevents incoming combat damage without preventing Shieldmage's outgoing damage")
    void protectsFromCombatDamage() {
        castShieldmage();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Ethersworn Shieldmage");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
