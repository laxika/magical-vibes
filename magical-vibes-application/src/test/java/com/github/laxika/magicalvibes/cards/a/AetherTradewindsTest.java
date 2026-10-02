package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Malfunction;
import com.github.laxika.magicalvibes.cards.s.SageOfShailasClaim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherTradewinds.class, GrizzlyBears.class, LlanowarElves.class,
        Malfunction.class, SageOfShailasClaim.class})
class AetherTradewindsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one permanent you control and one you don't control to their owners' hands")
    void returnsBothTargetedPermanentsToTheirOwnersHands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(ownPermanentId, opposingPermanentId));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Requires the first target to be a permanent you control")
    void requiresFirstTargetToBeControlledByCaster() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opposingPermanentId, ownPermanentId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Requires the second target to be a permanent you don't control")
    void requiresSecondTargetNotToBeControlledByCaster() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        UUID firstPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondPermanentId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstPermanentId, secondPermanentId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you don't control");
    }

    @Test
    @DisplayName("Returns a creature and the opponent's Aura attached to it together")
    void returnsCreatureAndOpposingAttachedAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SageOfShailasClaim());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Malfunction());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), aura.getId()));

        harness.assertInHand(player1, "Sage of Shaila's Claim");
        harness.assertInHand(player2, "Malfunction");
        harness.assertNotInGraveyard(player2, "Malfunction");
        harness.assertNotOnBattlefield(player1, "Sage of Shaila's Claim");
        harness.assertNotOnBattlefield(player2, "Malfunction");
    }

    @Test
    @DisplayName("Returns the opponent's permanent when the caster's target leaves before resolution")
    void returnsOpponentTargetWhenOwnTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SageOfShailasClaim());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SageOfShailasClaim());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();
        harness.castInstant(player1, 0, List.of(own.getId(), opposing.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, own);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sage of Shaila's Claim");
        harness.assertNotInHand(player1, "Sage of Shaila's Claim");
        harness.assertInHand(player2, "Sage of Shaila's Claim");
        harness.assertNotOnBattlefield(player2, "Sage of Shaila's Claim");
    }

    @Test
    @DisplayName("Returns the caster's permanent when the opponent's target leaves before resolution")
    void returnsOwnTargetWhenOpponentTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SageOfShailasClaim());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SageOfShailasClaim());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();
        harness.castInstant(player1, 0, List.of(own.getId(), opposing.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposing);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Sage of Shaila's Claim");
        harness.assertNotOnBattlefield(player1, "Sage of Shaila's Claim");
        harness.assertInGraveyard(player2, "Sage of Shaila's Claim");
        harness.assertNotInHand(player2, "Sage of Shaila's Claim");
    }

    @Test
    @DisplayName("Does not resolve when both targets have left the battlefield")
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SageOfShailasClaim());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SageOfShailasClaim());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();
        harness.castInstant(player1, 0, List.of(own.getId(), opposing.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, own);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposing);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sage of Shaila's Claim");
        harness.assertInGraveyard(player2, "Sage of Shaila's Claim");
        harness.assertNotInHand(player1, "Sage of Shaila's Claim");
        harness.assertNotInHand(player2, "Sage of Shaila's Claim");
        harness.assertInGraveyard(player1, "Aether Tradewinds");
    }

    @Test
    @DisplayName("Returns controlled permanents to their owners rather than their controllers")
    void returnsStolenPermanentToOwner() {
        SageOfShailasClaim card = new SageOfShailasClaim();
        card.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SageOfShailasClaim());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        harness.castAndResolveInstant(player1, 0, List.of(stolen.getId(), opposing.getId()));

        harness.assertNotInHand(player1, "Sage of Shaila's Claim");
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(stolen.getCard(), opposing.getCard());
        harness.assertNotOnBattlefield(player1, "Sage of Shaila's Claim");
        harness.assertNotOnBattlefield(player2, "Sage of Shaila's Claim");
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
