package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RescueTheFoal;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PegasusGuardian.class, RescueTheFoal.class, SteadfastPaladin.class})
class PegasusGuardianTest extends BaseCardTest {

    @Test
    void createsPegasusAtEndStepAfterPermanentYouControlledLeft() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalTargetId = target.getId();
        harness.castAdventure(player1, 0, originalTargetId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Steadfast Paladin")).isNotEqualTo(originalTargetId);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pegasus");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void doesNotCreatePegasusWithoutAQualifyingPermanentLeaving() {
        harness.addToBattlefield(player1, new PegasusGuardian());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pegasus");
    }

    @Test
    void rescueTheFoalTargetsOnlyCreatureYouControl() {
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin()).getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pegasus Guardian");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void multipleDeparturesCreateOnlyOneToken() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
        harness.getPermanentRemovalService().removePermanentToHand(gd, second);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Pegasus"))
                .hasSize(1);
    }

    @Test
    void opponentsPermanentLeavingDoesNotQualify() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opponentCreature);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pegasus");
    }

    @Test
    void departureDuringOpponentsTurnDoesNotCarryIntoYourTurn() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        harness.forceActivePlayer(player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Pegasus");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pegasus");
    }

    @Test
    void departureAfterEndStepBeginsDoesNotTriggerRetroactively() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of(new PegasusGuardian()));

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pegasus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureReturnsBorrowedCreatureToItsOwner() {
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        harness.setHand(player1, List.of(new PegasusGuardian()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAdventure(player1, 0, borrowed.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Steadfast Paladin");
        harness.assertOnBattlefield(player2, "Steadfast Paladin");
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardInsteadOfAdventureExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAdventure(player1, 0, creature.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void triggeredAbilityStillCreatesTokenAfterGuardianLeaves() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new PegasusGuardian());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guardian);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pegasus");
        harness.assertNotOnBattlefield(player1, "Pegasus Guardian");
    }

    @Test
    void rescueTheFoalExilesCreatureTokenWithoutReturningIt() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        UUID tokenId = harness.getPermanentId(player1, "Pegasus");
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAdventure(player1, 0, tokenId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pegasus");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
