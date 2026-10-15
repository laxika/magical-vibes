package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandSleuth.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class WoodlandSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Does not return a creature card without morbid")
    void noReturnWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // No morbid — no ETB trigger should fire
        assertThat(gd.stack).isEmpty();
        // Grizzly Bears stays in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a creature card at random from graveyard when morbid is met")
    void returnsCreatureWithMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Simulate morbid
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB trigger goes on stack)
        harness.passBothPriorities(); // resolve ETB trigger

        // Grizzly Bears should be in hand
        harness.assertInHand(player1, "Grizzly Bears");
        // Graveyard should be empty (only creature was returned)
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only returns creature cards, not non-creature cards")
    void onlyReturnsCreatureCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        // Put only a non-creature card (Shock) in graveyard
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Shock should stay in graveyard — it's not a creature
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when graveyard has no creature cards and morbid is met")
    void doesNothingWithEmptyGraveyardAndMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Killing a creature with Shock enables morbid to return a creature from graveyard")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new Shock(), new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill Grizzly Bears with Shock
        java.util.UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Now cast Woodland Sleuth — morbid should be active
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Llanowar Elves should be returned to hand
        harness.assertInHand(player1, "Llanowar Elves");

        // Woodland Sleuth should be on the battlefield
        harness.assertOnBattlefield(player1, "Woodland Sleuth");
    }

    @Test
    @DisplayName("Returns exactly one eligible creature from only the controller's graveyard")
    void returnsExactlyOneCreatureFromOwnGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears opposingCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, elves, shock));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.setHand(player1, List.of(new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(bears, elves);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(shock);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can return Woodland Sleuth itself if it dies before its trigger resolves")
    void canReturnItselfAfterDyingInResponse() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WoodlandSleuth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        java.util.UUID sleuthId = harness.getPermanentId(player1, "Woodland Sleuth");
        harness.castAndResolveInstant(player2, 0, sleuthId);
        harness.castAndResolveInstant(player2, 0, sleuthId);
        harness.assertInGraveyard(player1, "Woodland Sleuth");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Woodland Sleuth");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
