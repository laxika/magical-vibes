package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoRestForTheWicked.class, GrizzlyBears.class, Shock.class, Naturalize.class, RavenousRats.class})
class NoRestForTheWickedTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices No Rest for the Wicked and puts the ability on the stack")
    void activationSacrificesAndStacksAbility() {
        harness.addToBattlefield(player1, new NoRestForTheWicked());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "No Rest for the Wicked");
        harness.assertInGraveyard(player1, "No Rest for the Wicked");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Returns only creature cards that were put into your graveyard from battlefield this turn")
    void returnsOnlyThisTurnBattlefieldCreaturesFromYourGraveyard() {
        Card alreadyInGraveyard = new GrizzlyBears();
        Card diedThisTurn = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        harness.addToBattlefield(player1, new NoRestForTheWicked());
        UUID targetCreatureId = harness.addToBattlefieldAndReturn(player1, diedThisTurn).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(alreadyInGraveyard.getId()));
    }

    @Test
    @DisplayName("Does not return creatures put into an opponent's graveyard this turn")
    void doesNotReturnOpponentsCreatureCards() {
        Card opponentsCreature = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        UUID targetCreatureId = harness.addToBattlefieldAndReturn(player2, opponentsCreature).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(opponentsCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentsCreature.getId()));
    }

    @Test
    @DisplayName("Does not return creatures that died on a previous turn")
    void doesNotReturnCreaturesFromPreviousTurn() {
        Card diedLastTurn = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        UUID targetCreatureId = harness.addToBattlefieldAndReturn(player1, diedLastTurn).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int noRestIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "No Rest for the Wicked"));
        harness.activateAbility(player1, noRestIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(diedLastTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diedLastTurn.getId()));
    }

    @Test
    @DisplayName("Returns every matching creature card put into the graveyard this turn")
    void returnsAllMatchingCreatureCards() {
        Card firstDiedThisTurn = new GrizzlyBears();
        Card secondDiedThisTurn = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player1, firstDiedThisTurn).getId();
        harness.addToBattlefield(player1, secondDiedThisTurn);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, firstTargetId);
        harness.passBothPriorities();

        UUID secondTargetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, secondTargetId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstDiedThisTurn.getId(), secondDiedThisTurn.getId());
    }

    @Test
    @DisplayName("Does not return noncreature cards put into the graveyard from the battlefield")
    void doesNotReturnNoncreatureCards() {
        Card noncreaturePermanentCard = new NoRestForTheWicked();
        Card diedThisTurn = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        var noncreaturePermanent = harness.addToBattlefieldAndReturn(player1, noncreaturePermanentCard);
        var creaturePermanent = harness.addToBattlefieldAndReturn(player1, diedThisTurn);
        harness.setHand(player1, List.of(new Naturalize(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, noncreaturePermanent.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, creaturePermanent.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(diedThisTurn.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(noncreaturePermanentCard.getId());
    }

    @Test
    @DisplayName("Returns creatures that entered the graveyard before No Rest for the Wicked")
    void returnsCreaturesThatDiedBeforeNoRestEntered() {
        Card diedBeforeNoRestEntered = new GrizzlyBears();
        UUID targetCreatureId = harness.addToBattlefieldAndReturn(player1, diedBeforeNoRestEntered).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new NoRestForTheWicked());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(diedBeforeNoRestEntered.getId());
    }

    @Test
    @DisplayName("Does not return a creature card put into the graveyard from a nonbattlefield zone")
    void doesNotReturnCreatureCardsPutThereFromNonbattlefield() {
        Card discardedCreature = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        harness.setHand(player1, List.of(discardedCreature));
        harness.setHand(player2, List.of(new RavenousRats()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(discardedCreature.getId());
    }
}
