package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimalHuntbeast;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoaringPrimadox.class, PrimalHuntbeast.class, Forest.class})
class RoaringPrimadoxTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new RoaringPrimadox());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Roaring Primadox's upkeep ability");
    }

    @Test
    @DisplayName("Prompt only includes creatures you control")
    void promptOnlyIncludesCreaturesYouControl() {
        addCreatureReady(player1, new RoaringPrimadox());
        Permanent ownCreature = addCreatureReady(player1, new PrimalHuntbeast());
        Permanent opponentCreature = addCreatureReady(player2, new PrimalHuntbeast());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId(), land.getId());
    }

    @Test
    @DisplayName("Can choose itself when it is the only creature")
    void canChooseItselfWhenOnlyCreature() {
        Permanent primadox = addCreatureReady(player1, new RoaringPrimadox());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(primadox.getId());
        harness.handlePermanentChosen(player1, primadox.getId());

        harness.assertInHand(player1, "Roaring Primadox");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chosen creature is returned to its owner's hand")
    void chosenCreatureReturnedToOwnersHand() {
        addCreatureReady(player1, new RoaringPrimadox());
        Permanent creature = addCreatureReady(player1, new PrimalHuntbeast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        harness.assertInHand(player1, "Primal Huntbeast");
    }

    @Test
    @DisplayName("Triggered ability still returns a creature after Primadox leaves")
    void returnsCreatureAfterSourceLeaves() {
        Permanent primadox = addCreatureReady(player1, new RoaringPrimadox());
        Permanent creature = addCreatureReady(player1, new PrimalHuntbeast());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(primadox);
        gd.playerGraveyards.get(player1.getId()).add(primadox.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertInHand(player1, "Primal Huntbeast");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggered ability does nothing when no creatures remain")
    void resolvesWithNoCreaturesRemaining() {
        Permanent primadox = addCreatureReady(player1, new RoaringPrimadox());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(primadox);
        gd.playerGraveyards.get(player1.getId()).add(primadox.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature controlled by Primadox's controller returns to its actual owner")
    void returnsBorrowedCreatureToOwner() {
        addCreatureReady(player1, new RoaringPrimadox());
        Permanent borrowed = addCreatureReady(player1, new PrimalHuntbeast());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, borrowed.getId());

        harness.assertInHand(player2, "Primal Huntbeast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(borrowed.getId()));
    }
}
