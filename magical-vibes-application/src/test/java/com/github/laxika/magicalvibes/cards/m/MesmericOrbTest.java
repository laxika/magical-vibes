package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesmericOrb.class, Mountain.class})
class MesmericOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Each untapped permanent makes its controller mill a card")
    void eachUntappedPermanentMillsItsController() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player1Permanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        player1Permanent.tap();
        trimDeck(player1, 10);

        advanceToUpkeep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's untapped permanent makes that opponent mill a card")
    void opponentsUntappedPermanentMillsOpponent() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new Mountain());
        player2Permanent.tap();
        trimDeck(player2, 10);

        advanceToUpkeep(player2);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each permanent that becomes untapped creates a separate mill trigger")
    void eachBecomesUntappedEventCreatesSeparateMillTrigger() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent firstPermanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondPermanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        firstPermanent.tap();
        secondPermanent.tap();
        trimDeck(player1, 10);

        advanceToUpkeep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An already untapped permanent does not trigger Mesmeric Orb")
    void alreadyUntappedPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesmericOrb());
        harness.addToBattlefield(player1, new Mountain());
        trimDeck(player1, 10);

        advanceToUpkeep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller is remembered if the untapped permanent leaves before resolution")
    void remembersControllerIfPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new Mountain());
        player2Permanent.tap();
        trimDeck(player2, 10);

        harness.inMutationScope(() -> {
            player2Permanent.untap();
            harness.getTriggerCollectionService().checkBecomesUntappedTriggers(gd, player2Permanent);
        });
        gd.playerBattlefields.get(player2.getId()).remove(player2Permanent);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private void trimDeck(Player player, int size) {
        while (gd.playerDecks.get(player.getId()).size() > size) {
            gd.playerDecks.get(player.getId()).removeFirst();
        }
    }

}
