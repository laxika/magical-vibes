package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThievingAmalgam.class, GrizzlyBears.class})
class ThievingAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card of each opponent's library under your control")
    void manifestsTopCardOfOpponentsLibrary() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new ThievingAmalgam());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature you control but do not own dying drains its owner")
    void stolenCreatureDeathDrainsItsOwner() {
        harness.addToBattlefield(player1, new ThievingAmalgam());
        Card stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        stolenCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("A creature you both control and own dying does not trigger the drain")
    void ownCreatureDeathDoesNotDrain() {
        harness.addToBattlefield(player1, new ThievingAmalgam());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        ownCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }
}
