package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AkkiUnderling;
import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EiganjoFreeRiders.class, ArabaMothrider.class, AkkiUnderling.class})
class EiganjoFreeRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        addCreatureReady(player1, new EiganjoFreeRiders());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Prompt only includes white creatures you control")
    void promptOnlyIncludesWhiteCreaturesYouControl() {
        Permanent freeRiders = addCreatureReady(player1, new EiganjoFreeRiders());
        Permanent whiteCreature = addCreatureReady(player1, new ArabaMothrider());
        Permanent nonWhiteCreature = addCreatureReady(player1, new AkkiUnderling());
        Permanent opponentsWhiteCreature = addCreatureReady(player2, new ArabaMothrider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(freeRiders.getId(), whiteCreature.getId())
                .doesNotContain(nonWhiteCreature.getId(), opponentsWhiteCreature.getId());
    }

    @Test
    @DisplayName("Chosen white creature is returned to its owner's hand")
    void chosenWhiteCreatureReturnedToOwnersHand() {
        addCreatureReady(player1, new EiganjoFreeRiders());
        Permanent whiteCreature = addCreatureReady(player1, new ArabaMothrider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, whiteCreature.getId());

        harness.assertNotOnBattlefield(player1, "Araba Mothrider");
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Returns a controlled white creature to its owner's hand")
    void controlledWhiteCreatureReturnedToItsOwnersHand() {
        addCreatureReady(player1, new EiganjoFreeRiders());
        ArabaMothrider whiteCreatureCard = new ArabaMothrider();
        whiteCreatureCard.setOwnerId(player2.getId());
        Permanent whiteCreature = addCreatureReady(player1, whiteCreatureCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, whiteCreature.getId());

        harness.assertNotInHand(player1, "Araba Mothrider");
        harness.assertInHand(player2, "Araba Mothrider");
    }

    @Test
    @DisplayName("Must return Free-Riders itself when it is the only white creature")
    void returnsItselfWhenOnlyWhiteCreature() {
        Permanent freeRiders = addCreatureReady(player1, new EiganjoFreeRiders());
        addCreatureReady(player1, new AkkiUnderling());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(freeRiders.getId());
        harness.handlePermanentChosen(player1, freeRiders.getId());

        harness.assertNotOnBattlefield(player1, "Eiganjo Free-Riders");
        harness.assertInHand(player1, "Eiganjo Free-Riders");
        harness.assertOnBattlefield(player1, "Akki Underling");
    }

    @Test
    @DisplayName("Upkeep ability still returns another white creature after Free-Riders leaves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent freeRiders = addCreatureReady(player1, new EiganjoFreeRiders());
        Permanent whiteCreature = addCreatureReady(player1, new ArabaMothrider());

        advanceToUpkeep(player1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, freeRiders));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(whiteCreature.getId());
        harness.handlePermanentChosen(player1, whiteCreature.getId());

        harness.assertNotOnBattlefield(player1, "Araba Mothrider");
        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Does nothing when no controlled white creature remains at resolution")
    void doesNothingWithoutEligibleCreature() {
        Permanent freeRiders = addCreatureReady(player1, new EiganjoFreeRiders());
        addCreatureReady(player1, new AkkiUnderling());
        addCreatureReady(player2, new ArabaMothrider());

        advanceToUpkeep(player1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, freeRiders));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Akki Underling");
        harness.assertOnBattlefield(player2, "Araba Mothrider");
        harness.assertNotInHand(player1, "Akki Underling");
    }
}
